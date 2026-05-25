package com.hotel.seating.dto;

import com.hotel.seating.model.DiningTable;

import java.util.List;

public record AvailabilityResponse(
        int totalSeats,
        int reservedSeats,
        int availableSeats,
        double utilizationPercent,
        double queuePressure,
        boolean overbookingRisk,
        List<DiningTable> recommendedTables
) {
}

