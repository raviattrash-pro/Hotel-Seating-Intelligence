package com.hotel.seating.repository;

import com.hotel.seating.model.Booking;
import com.hotel.seating.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookingDateOrderByBookingTimeAsc(LocalDate date);
    List<Booking> findByBookingDateAndBookingTimeAndStatusIn(LocalDate date, LocalTime time, Collection<BookingStatus> statuses);
    List<Booking> findByBookingDateBetween(LocalDate start, LocalDate end);
    List<Booking> findByPhoneIn(Collection<String> phones);
}
