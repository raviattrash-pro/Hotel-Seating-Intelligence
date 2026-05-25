package com.hotel.seating.repository;

import com.hotel.seating.model.FloorPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FloorPlanRepository extends JpaRepository<FloorPlan, Long> {
    List<FloorPlan> findAllByOrderByIdAsc();
}

