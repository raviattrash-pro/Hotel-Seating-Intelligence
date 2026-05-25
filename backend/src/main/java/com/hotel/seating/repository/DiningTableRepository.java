package com.hotel.seating.repository;

import com.hotel.seating.model.DiningTable;
import com.hotel.seating.model.SeatingZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {
    List<DiningTable> findByZoneOrderByCapacityAsc(SeatingZone zone);
    List<DiningTable> findAllByOrderByLabelAsc();
    List<DiningTable> findByFloorPlanIdOrderByLabelAsc(Long floorPlanId);
    long countByFloorPlanId(Long floorPlanId);
}
