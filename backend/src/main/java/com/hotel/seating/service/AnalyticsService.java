package com.hotel.seating.service;

import com.hotel.seating.dto.DashboardResponse;
import com.hotel.seating.model.Booking;
import com.hotel.seating.model.BookingStatus;
import com.hotel.seating.model.DiningTable;
import com.hotel.seating.repository.BookingRepository;
import com.hotel.seating.repository.DiningTableRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    private static final Set<BookingStatus> ACTIVE_STATUSES = Set.of(
            BookingStatus.PAYMENT_PENDING,
            BookingStatus.RESERVED,
            BookingStatus.SEATED,
            BookingStatus.WAITLISTED
    );

    private final BookingRepository bookingRepository;
    private final DiningTableRepository tableRepository;

    public AnalyticsService(BookingRepository bookingRepository, DiningTableRepository tableRepository) {
        this.bookingRepository = bookingRepository;
        this.tableRepository = tableRepository;
    }

    public DashboardResponse dashboard(LocalDate date) {
        List<DiningTable> tables = tableRepository.findAll();
        int totalSeats = tables.stream().mapToInt(DiningTable::getCapacity).sum();
        List<Booking> todayBookings = bookingRepository.findByBookingDateOrderByBookingTimeAsc(date);
        List<Booking> activeToday = todayBookings.stream()
                .filter(booking -> ACTIVE_STATUSES.contains(booking.getStatus()))
                .toList();
        int reservedSeats = activeToday.stream().mapToInt(Booking::getPartySize).sum();
        double todayUtilization = percent(reservedSeats, totalSeats);
        double predictedUtilization = predictedUtilization(date, totalSeats);

        Map<String, Integer> seatsBySlot = activeToday.stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getBookingTime().toString(),
                        TreeMap::new,
                        Collectors.summingInt(Booking::getPartySize)
                ));

        List<DashboardResponse.SlotInsight> slots = seatsBySlot.entrySet().stream()
                .map(entry -> slotInsight(entry.getKey(), entry.getValue(), activeToday, totalSeats))
                .sorted(Comparator.comparing(DashboardResponse.SlotInsight::time))
                .toList();

        Map<String, Long> zoneMix = activeToday.stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getPreferredZone().name(),
                        TreeMap::new,
                        Collectors.counting()
                ));

        int refundableDeposits = activeToday.stream()
                .filter(booking -> !booking.isDepositRefunded())
                .mapToInt(Booking::getDepositAmountInr)
                .sum();

        return new DashboardResponse(
                date,
                totalSeats,
                reservedSeats,
                todayUtilization,
                predictedUtilization,
                activeToday.size(),
                (int) activeToday.stream().filter(booking -> booking.getStatus() == BookingStatus.WAITLISTED).count(),
                refundableDeposits,
                slots,
                zoneMix,
                recommendations(todayUtilization, predictedUtilization, slots)
        );
    }

    private DashboardResponse.SlotInsight slotInsight(String time, int reservedSeats, List<Booking> activeToday, int totalSeats) {
        double utilization = percent(reservedSeats, totalSeats);
        long sameSlotBookings = activeToday.stream()
                .filter(booking -> booking.getBookingTime().equals(LocalTime.parse(time)))
                .count();
        double serviceCapacity = Math.max(totalSeats / 4.0, 1);
        double queuePressure = Math.min(1.5, sameSlotBookings / serviceCapacity);
        return new DashboardResponse.SlotInsight(
                time,
                reservedSeats,
                utilization,
                Math.round(queuePressure * 100.0) / 100.0,
                utilization > 100,
                utilization < 45
        );
    }

    private double predictedUtilization(LocalDate date, int totalSeats) {
        LocalDate start = date.minusDays(28);
        List<Booking> history = bookingRepository.findByBookingDateBetween(start, date.minusDays(1));
        if (history.isEmpty() || totalSeats == 0) {
            return 0;
        }
        Map<LocalDate, Integer> seatsByDate = history.stream()
                .filter(booking -> ACTIVE_STATUSES.contains(booking.getStatus()))
                .collect(Collectors.groupingBy(
                        Booking::getBookingDate,
                        Collectors.summingInt(Booking::getPartySize)
                ));
        double averageSeats = seatsByDate.values().stream().mapToInt(Integer::intValue).average().orElse(0);
        return percent((int) Math.round(averageSeats), totalSeats);
    }

    private List<String> recommendations(double todayUtilization,
                                         double predictedUtilization,
                                         List<DashboardResponse.SlotInsight> slots) {
        java.util.ArrayList<String> output = new java.util.ArrayList<>();
        if (todayUtilization > 95 || slots.stream().anyMatch(DashboardResponse.SlotInsight::overbooked)) {
            output.add("High booking pressure detected. Hold a few flexible tables for walk-ins and family reallocation.");
        }
        if (todayUtilization < 45 || predictedUtilization < 50) {
            output.add("Low-crowd window found. Run off-peak offers for underused slots.");
        }
        if (slots.stream().anyMatch(slot -> slot.queuePressure() > 0.8)) {
            output.add("Queue pressure is high in at least one slot. Increase turn-time attention and avoid assigning smoking/bar tables to family guests.");
        }
        if (output.isEmpty()) {
            output.add("Utilization is balanced. Keep deposits refundable on arrival to reduce fake bookings while protecting table availability.");
        }
        return output;
    }

    private double percent(int value, int total) {
        if (total == 0) {
            return 0;
        }
        return Math.round((value * 10000.0 / total)) / 100.0;
    }
}
