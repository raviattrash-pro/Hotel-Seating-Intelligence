package com.hotel.seating.dto;

import com.hotel.seating.model.BookingSource;
import com.hotel.seating.model.SeatingZone;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record BookingRequest(
        @NotBlank String guestName,
        @NotBlank String phone,
        @Min(1) int partySize,
        @NotNull LocalDate bookingDate,
        @NotNull LocalTime bookingTime,
        @NotNull SeatingZone preferredZone,
        @NotNull BookingSource source,
        Long requestedTableId,
        String paymentTransactionId,
        String paymentScreenshotUrl
) {
}
