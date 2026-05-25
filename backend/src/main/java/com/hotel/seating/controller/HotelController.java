package com.hotel.seating.controller;

import com.hotel.seating.dto.AvailabilityResponse;
import com.hotel.seating.dto.BookingRequest;
import com.hotel.seating.dto.DashboardResponse;
import com.hotel.seating.model.Booking;
import com.hotel.seating.model.DiningTable;
import com.hotel.seating.model.FloorPlan;
import com.hotel.seating.model.HotelConfig;
import com.hotel.seating.model.SeatingZone;
import com.hotel.seating.model.TableStatus;
import com.hotel.seating.repository.DiningTableRepository;
import com.hotel.seating.repository.FloorPlanRepository;
import com.hotel.seating.service.AnalyticsService;
import com.hotel.seating.service.BookingService;
import com.hotel.seating.service.HotelConfigService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HotelController {
    private final HotelConfigService configService;
    private final DiningTableRepository tableRepository;
    private final FloorPlanRepository floorPlanRepository;
    private final BookingService bookingService;
    private final AnalyticsService analyticsService;

    public HotelController(HotelConfigService configService,
                           DiningTableRepository tableRepository,
                           FloorPlanRepository floorPlanRepository,
                           BookingService bookingService,
                           AnalyticsService analyticsService) {
        this.configService = configService;
        this.tableRepository = tableRepository;
        this.floorPlanRepository = floorPlanRepository;
        this.bookingService = bookingService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/config")
    HotelConfig config() {
        return configService.getConfig();
    }

    @PutMapping("/config")
    HotelConfig updateConfig(@Valid @RequestBody HotelConfig config) {
        return configService.update(config);
    }

    @GetMapping("/tables")
    List<DiningTable> tables(@RequestParam(required = false) Long floorId) {
        return floorId == null
                ? tableRepository.findAllByOrderByLabelAsc()
                : tableRepository.findByFloorPlanIdOrderByLabelAsc(floorId);
    }

    @GetMapping("/floors")
    List<FloorPlan> floors() {
        return floorPlanRepository.findAllByOrderByIdAsc();
    }

    @PostMapping("/floors")
    FloorPlan createFloor(@Valid @RequestBody FloorPlan floorPlan) {
        FloorPlan saved = floorPlanRepository.save(floorPlan);
        ensureDefaultTables(saved);
        return saved;
    }

    @PutMapping("/floors/{id}")
    FloorPlan updateFloor(@PathVariable Long id, @Valid @RequestBody FloorPlan incoming) {
        FloorPlan floorPlan = floorPlanRepository.findById(id).orElseThrow();
        floorPlan.setName(incoming.getName());
        floorPlan.setImageUrl(incoming.getImageUrl());
        floorPlan.setActive(incoming.isActive());
        FloorPlan saved = floorPlanRepository.save(floorPlan);
        ensureDefaultTables(saved);
        return saved;
    }

    @PutMapping("/tables/{id}")
    DiningTable updateTable(@PathVariable Long id, @Valid @RequestBody DiningTable incoming) {
        DiningTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found"));
        table.setLabel(incoming.getLabel());
        table.setCapacity(incoming.getCapacity());
        table.setZone(incoming.getZone());
        table.setStatus(incoming.getStatus());
        if (incoming.getFloorPlan() != null && incoming.getFloorPlan().getId() != null) {
            FloorPlan floorPlan = floorPlanRepository.findById(incoming.getFloorPlan().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Floor not found"));
            table.setFloorPlan(floorPlan);
        }
        table.setXPercent(incoming.getXPercent());
        table.setYPercent(incoming.getYPercent());
        table.setDisplayWidth(incoming.getDisplayWidth());
        table.setDisplayHeight(incoming.getDisplayHeight());
        table.setCombinable(incoming.isCombinable());
        return tableRepository.save(table);
    }

    @PostMapping("/tables")
    DiningTable createTable(@Valid @RequestBody DiningTable incoming) {
        return tableRepository.save(incoming);
    }

    @DeleteMapping("/tables/{id}")
    void deleteTable(@PathVariable Long id) {
        DiningTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found"));
        table.setStatus(TableStatus.BLOCKED);
        tableRepository.save(table);
    }

    @PostMapping("/floors/{id}/generate-layout")
    List<DiningTable> generateLayout(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        FloorPlan floorPlan = floorPlanRepository.findById(id).orElseThrow();
        int tableCount = Math.max(1, Math.min(request.getOrDefault("tableCount", 12), 80));
        int chairCount = Math.max(1, Math.min(request.getOrDefault("chairCount", 4), 20));
        List<DiningTable> existingTables = tableRepository.findByFloorPlanIdOrderByLabelAsc(id);

        int columns = (int) Math.ceil(Math.sqrt(tableCount));
        int rows = (int) Math.ceil(tableCount / (double) columns);
        java.util.ArrayList<DiningTable> generated = new java.util.ArrayList<>();
        for (int index = 0; index < tableCount; index++) {
            int row = index / columns;
            int column = index % columns;
            int x = (int) Math.round(((column + 1) * 100.0) / (columns + 1));
            int y = (int) Math.round(((row + 1) * 100.0) / (rows + 1));
            SeatingZone zone = index % 5 == 0 ? SeatingZone.FAMILY : SeatingZone.GENERAL;
            DiningTable diningTable = index < existingTables.size() ? existingTables.get(index) : new DiningTable();
            diningTable.setFloorPlan(floorPlan);
            diningTable.setLabel("T" + (index + 1));
            diningTable.setCapacity(chairCount);
            diningTable.setZone(zone);
            diningTable.setStatus(TableStatus.AVAILABLE);
            diningTable.setXPercent(x);
            diningTable.setYPercent(y);
            diningTable.setDisplayWidth(chairCount >= 6 ? 72 : 58);
            diningTable.setDisplayHeight(chairCount >= 6 ? 54 : 48);
            diningTable.setCombinable(true);
            generated.add(diningTable);
        }
        for (int index = tableCount; index < existingTables.size(); index++) {
            DiningTable diningTable = existingTables.get(index);
            diningTable.setStatus(TableStatus.BLOCKED);
            generated.add(diningTable);
        }
        return tableRepository.saveAll(generated);
    }

    @GetMapping("/bookings")
    List<Booking> bookings(@RequestParam(required = false) LocalDate date) {
        return bookingService.bookingsFor(date == null ? LocalDate.now() : date);
    }

    @PostMapping("/bookings")
    Booking createBooking(@Valid @RequestBody BookingRequest request) {
        return bookingService.create(request);
    }

    @PostMapping("/bookings/{id}/check-in")
    Booking checkIn(@PathVariable Long id) {
        return bookingService.checkIn(id);
    }

    @PostMapping("/bookings/{id}/verify-payment")
    Booking verifyPayment(@PathVariable Long id) {
        return bookingService.verifyPayment(id);
    }

    @PostMapping("/bookings/{id}/cancel")
    Booking cancel(@PathVariable Long id) {
        return bookingService.cancel(id);
    }

    @GetMapping("/availability")
    AvailabilityResponse availability(@RequestParam LocalDate date,
                                      @RequestParam LocalTime time,
                                      @RequestParam int partySize,
                                      @RequestParam(required = false) String zone,
                                      @RequestParam(required = false) Long floorId) {
        return bookingService.availability(date, time, partySize, zone, floorId);
    }

    @GetMapping("/dashboard")
    DashboardResponse dashboard(@RequestParam(required = false) LocalDate date) {
        return analyticsService.dashboard(date == null ? LocalDate.now() : date);
    }

    private void ensureDefaultTables(FloorPlan floorPlan) {
        if (tableRepository.countByFloorPlanId(floorPlan.getId()) > 0) {
            return;
        }
        tableRepository.saveAll(List.of(
                table(floorPlan, "F1", 4, SeatingZone.FAMILY, 18, 22, true),
                table(floorPlan, "F2", 4, SeatingZone.FAMILY, 34, 22, true),
                table(floorPlan, "F3", 6, SeatingZone.FAMILY, 24, 43, true),
                table(floorPlan, "P1", 8, SeatingZone.PRIVATE, 52, 24, false),
                table(floorPlan, "G1", 2, SeatingZone.GENERAL, 64, 44, true),
                table(floorPlan, "G2", 4, SeatingZone.GENERAL, 78, 44, true),
                table(floorPlan, "B1", 2, SeatingZone.BAR, 70, 18, false),
                table(floorPlan, "B2", 2, SeatingZone.BAR, 83, 18, false),
                table(floorPlan, "S1", 4, SeatingZone.SMOKING, 17, 72, true),
                table(floorPlan, "S2", 6, SeatingZone.SMOKING, 34, 72, true),
                table(floorPlan, "O1", 4, SeatingZone.OUTDOOR, 63, 75, true),
                table(floorPlan, "O2", 4, SeatingZone.OUTDOOR, 80, 75, true)
        ));
    }

    private DiningTable table(FloorPlan floorPlan, String label, int capacity, SeatingZone zone, int x, int y, boolean combinable) {
        DiningTable table = new DiningTable();
        table.setFloorPlan(floorPlan);
        table.setLabel(label);
        table.setCapacity(capacity);
        table.setZone(zone);
        table.setXPercent(x);
        table.setYPercent(y);
        table.setDisplayWidth(capacity >= 6 ? 72 : 58);
        table.setDisplayHeight(capacity >= 6 ? 54 : 48);
        table.setCombinable(combinable);
        return table;
    }
}
