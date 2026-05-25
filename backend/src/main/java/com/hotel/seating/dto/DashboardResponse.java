package com.hotel.seating.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
        LocalDate date,
        int totalSeats,
        int todayReservedSeats,
        double todayUtilizationPercent,
        double predictedUtilizationPercent,
        int activeBookings,
        int waitlistedBookings,
        int refundableDepositsInr,
        List<SlotInsight> slots,
        Map<String, Long> zoneMix,
        List<String> recommendations
) {
    public record SlotInsight(
            String time,
            int reservedSeats,
            double utilizationPercent,
            double queuePressure,
            boolean overbooked,
            boolean offerRecommended
    ) {
    }
}

