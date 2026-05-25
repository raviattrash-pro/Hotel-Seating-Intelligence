package com.hotel.seating.service;

import com.hotel.seating.dto.AvailabilityResponse;
import com.hotel.seating.dto.BookingRequest;
import com.hotel.seating.model.Booking;
import com.hotel.seating.model.BookingStatus;
import com.hotel.seating.model.DiningTable;
import com.hotel.seating.model.TableStatus;
import com.hotel.seating.repository.BookingRepository;
import com.hotel.seating.repository.DiningTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class BookingService {
    private static final Set<BookingStatus> ACTIVE_STATUSES = EnumSet.of(
            BookingStatus.PAYMENT_PENDING,
            BookingStatus.RESERVED,
            BookingStatus.SEATED,
            BookingStatus.WAITLISTED
    );

    private final BookingRepository bookingRepository;
    private final DiningTableRepository tableRepository;
    private final HotelConfigService configService;

    public BookingService(BookingRepository bookingRepository,
                          DiningTableRepository tableRepository,
                          HotelConfigService configService) {
        this.bookingRepository = bookingRepository;
        this.tableRepository = tableRepository;
        this.configService = configService;
    }

    public List<Booking> bookingsFor(LocalDate date) {
        return bookingRepository.findByBookingDateOrderByBookingTimeAsc(date);
    }

    public AvailabilityResponse availability(LocalDate date, LocalTime time, int partySize, String zoneName) {
        return availability(date, time, partySize, zoneName, null);
    }

    public AvailabilityResponse availability(LocalDate date, LocalTime time, int partySize, String zoneName, Long floorPlanId) {
        List<DiningTable> tables = floorPlanId == null
                ? tableRepository.findAllByOrderByLabelAsc()
                : tableRepository.findByFloorPlanIdOrderByLabelAsc(floorPlanId);
        List<Booking> activeBookings = bookingRepository.findByBookingDateAndBookingTimeAndStatusIn(date, time, ACTIVE_STATUSES);
        int totalSeats = tables.stream().mapToInt(DiningTable::getCapacity).sum();
        int reservedSeats = activeBookings.stream()
                .filter(booking -> booking.getDiningTable() != null)
                .mapToInt(booking -> booking.getDiningTable().getCapacity())
                .sum();
        double utilization = percent(reservedSeats, totalSeats);
        double queuePressure = queuePressure(activeBookings.size(), tables.size(), utilization);

        List<Long> bookedTableIds = activeBookings.stream()
                .filter(booking -> booking.getDiningTable() != null)
                .map(booking -> booking.getDiningTable().getId())
                .toList();

        List<DiningTable> recommended = tables.stream()
                .filter(table -> !bookedTableIds.contains(table.getId()))
                .filter(table -> table.getStatus() != TableStatus.BLOCKED)
                .filter(table -> table.getCapacity() >= partySize)
                .filter(table -> zoneName == null || zoneName.isBlank() || table.getZone().name().equalsIgnoreCase(zoneName))
                .sorted(Comparator.comparingInt(DiningTable::getCapacity))
                .limit(6)
                .toList();

        int overbookingLimit = totalSeats + (totalSeats * configService.getConfig().getOverbookingPercent() / 100);
        return new AvailabilityResponse(
                totalSeats,
                reservedSeats,
                Math.max(totalSeats - reservedSeats, 0),
                utilization,
                queuePressure,
                reservedSeats + partySize > overbookingLimit,
                recommended
        );
    }

    @Transactional
    public Booking create(BookingRequest request) {
        AvailabilityResponse availability = availability(
                request.bookingDate(),
                request.bookingTime(),
                request.partySize(),
                request.preferredZone().name(),
                null
        );

        DiningTable assignedTable = chooseTable(request, availability.recommendedTables());
        Booking booking = new Booking();
        booking.setGuestName(request.guestName());
        booking.setPhone(request.phone());
        booking.setPartySize(request.partySize());
        booking.setBookingDate(request.bookingDate());
        booking.setBookingTime(request.bookingTime());
        booking.setPreferredZone(request.preferredZone());
        booking.setSource(request.source());
        booking.setDepositAmountInr(configService.getConfig().getBookingDepositInr());
        booking.setPaymentTransactionId(request.paymentTransactionId());
        booking.setPaymentScreenshotUrl(request.paymentScreenshotUrl());
        booking.setDiningTable(assignedTable);
        booking.setStatus(assignedTable == null || availability.overbookingRisk()
                ? BookingStatus.WAITLISTED
                : BookingStatus.PAYMENT_PENDING);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking verifyPayment(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow();
        booking.setPaymentVerified(true);
        if (booking.getDiningTable() != null) {
            booking.setStatus(BookingStatus.RESERVED);
        }
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking checkIn(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow();
        booking.setStatus(BookingStatus.SEATED);
        booking.setDepositRefunded(true);
        booking.setCheckedInAt(java.time.LocalDateTime.now());
        if (booking.getDiningTable() != null) {
            booking.getDiningTable().setStatus(TableStatus.OCCUPIED);
        }
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancel(Long id) {
        Booking booking = bookingRepository.findById(id).orElseThrow();
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    private DiningTable chooseTable(BookingRequest request, List<DiningTable> recommendedTables) {
        if (request.requestedTableId() != null) {
            return recommendedTables.stream()
                    .filter(table -> table.getId().equals(request.requestedTableId()))
                    .findFirst()
                    .orElse(null);
        }
        return recommendedTables.stream().findFirst().orElse(null);
    }

    private double queuePressure(int activeBookings, int tableCount, double utilization) {
        if (tableCount == 0) {
            return 0;
        }
        double trafficIntensity = activeBookings / (tableCount * 1.0);
        return Math.min(1.5, (trafficIntensity + utilization / 100.0) / 2.0);
    }

    private double percent(int value, int total) {
        if (total == 0) {
            return 0;
        }
        return Math.round((value * 10000.0 / total)) / 100.0;
    }
}
