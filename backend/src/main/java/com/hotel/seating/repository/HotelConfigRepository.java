package com.hotel.seating.repository;

import com.hotel.seating.model.HotelConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelConfigRepository extends JpaRepository<HotelConfig, Long> {
}

