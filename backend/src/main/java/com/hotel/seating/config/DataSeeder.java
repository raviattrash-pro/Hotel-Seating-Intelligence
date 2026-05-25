package com.hotel.seating.config;

import com.hotel.seating.model.Booking;
import com.hotel.seating.model.BookingSource;
import com.hotel.seating.model.BookingStatus;
import com.hotel.seating.model.DiningTable;
import com.hotel.seating.model.FloorPlan;
import com.hotel.seating.model.HotelConfig;
import com.hotel.seating.model.SeatingZone;
import com.hotel.seating.repository.BookingRepository;
import com.hotel.seating.repository.DiningTableRepository;
import com.hotel.seating.repository.FloorPlanRepository;
import com.hotel.seating.repository.HotelConfigRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(HotelConfigRepository configRepository,
                               FloorPlanRepository floorPlanRepository,
                               DiningTableRepository tableRepository,
                               BookingRepository bookingRepository) {
        return args -> {
            if (configRepository.count() == 0) {
                HotelConfig config = new HotelConfig();
                config.setHotelName("Royal Spice Hotel");
                config.setFloorPlanImageUrl("");
                config.setUpiId("royalspice@upi");
                config.setQrCodeImageUrl("");
                config.setBookingDepositInr(50);
                config.setOverbookingPercent(10);
                configRepository.save(config);
            }

            if (floorPlanRepository.count() == 0) {
                FloorPlan ground = new FloorPlan();
                ground.setName("Ground Floor");
                ground.setImageUrl("");
                floorPlanRepository.save(ground);

                FloorPlan terrace = new FloorPlan();
                terrace.setName("Terrace Floor");
                terrace.setImageUrl("");
                floorPlanRepository.save(terrace);
            }

            if (tableRepository.count() == 0) {
                List<FloorPlan> floors = floorPlanRepository.findAllByOrderByIdAsc();
                FloorPlan ground = floors.get(0);
                FloorPlan terrace = floors.size() > 1 ? floors.get(1) : ground;
                tableRepository.saveAll(List.of(
                        table("F1", 4, SeatingZone.FAMILY, 18, 22, true, ground),
                        table("F2", 4, SeatingZone.FAMILY, 34, 22, true, ground),
                        table("F3", 6, SeatingZone.FAMILY, 24, 43, true, ground),
                        table("P1", 8, SeatingZone.PRIVATE, 52, 24, false, ground),
                        table("G1", 2, SeatingZone.GENERAL, 64, 44, true, ground),
                        table("G2", 4, SeatingZone.GENERAL, 78, 44, true, ground),
                        table("B1", 2, SeatingZone.BAR, 70, 18, false, ground),
                        table("B2", 2, SeatingZone.BAR, 83, 18, false, ground),
                        table("S1", 4, SeatingZone.SMOKING, 17, 72, true, terrace),
                        table("S2", 6, SeatingZone.SMOKING, 34, 72, true, terrace),
                        table("O1", 4, SeatingZone.OUTDOOR, 63, 75, true, terrace),
                        table("O2", 4, SeatingZone.OUTDOOR, 80, 75, true, terrace)
                ));
            }

            bookingRepository.deleteAll(bookingRepository.findByPhoneIn(List.of(
                    "9990001111",
                    "9990002222",
                    "9990003333",
                    "9990004444"
            )));

            if (bookingRepository.count() == 0) {
                LocalDate today = LocalDate.now();
                for (int i = 1; i <= 20; i++) {
                    Booking history = seed("History Guest " + i, "98000" + i, 2 + (i % 5), today.minusDays(i), i % 2 == 0 ? "19:00" : "20:30", SeatingZone.GENERAL, BookingSource.ONLINE, null);
                    history.setStatus(BookingStatus.COMPLETED);
                    history.setDepositRefunded(true);
                    bookingRepository.save(history);
                }
            }
        };
    }

    private DiningTable table(String label, int capacity, SeatingZone zone, int x, int y, boolean combinable, FloorPlan floorPlan) {
        DiningTable table = new DiningTable();
        table.setLabel(label);
        table.setCapacity(capacity);
        table.setZone(zone);
        table.setFloorPlan(floorPlan);
        table.setXPercent(x);
        table.setYPercent(y);
        table.setDisplayWidth(capacity >= 6 ? 72 : 58);
        table.setDisplayHeight(capacity >= 6 ? 54 : 48);
        table.setCombinable(combinable);
        return table;
    }

    private Booking seed(String guestName,
                         String phone,
                         int partySize,
                         LocalDate date,
                         String time,
                         SeatingZone zone,
                         BookingSource source,
                         DiningTable table) {
        Booking booking = new Booking();
        booking.setGuestName(guestName);
        booking.setPhone(phone);
        booking.setPartySize(partySize);
        booking.setBookingDate(date);
        booking.setBookingTime(LocalTime.parse(time));
        booking.setPreferredZone(zone);
        booking.setSource(source);
        booking.setDiningTable(table);
        booking.setStatus(BookingStatus.RESERVED);
        booking.setDepositAmountInr(50);
        return booking;
    }
}
