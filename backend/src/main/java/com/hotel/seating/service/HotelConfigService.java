package com.hotel.seating.service;

import com.hotel.seating.model.HotelConfig;
import com.hotel.seating.repository.HotelConfigRepository;
import org.springframework.stereotype.Service;

@Service
public class HotelConfigService {
    private final HotelConfigRepository repository;

    public HotelConfigService(HotelConfigRepository repository) {
        this.repository = repository;
    }

    public HotelConfig getConfig() {
        return repository.findAll().stream().findFirst().orElseGet(() -> {
            HotelConfig config = new HotelConfig();
            config.setHotelName("Hotel Seating Intelligence");
            config.setFloorPlanImageUrl("");
            return repository.save(config);
        });
    }

    public HotelConfig update(HotelConfig incoming) {
        HotelConfig config = getConfig();
        config.setHotelName(incoming.getHotelName());
        config.setFloorPlanImageUrl(incoming.getFloorPlanImageUrl());
        config.setUpiId(incoming.getUpiId());
        config.setQrCodeImageUrl(incoming.getQrCodeImageUrl());
        config.setBookingDepositInr(incoming.getBookingDepositInr());
        config.setSlotMinutes(incoming.getSlotMinutes());
        config.setOverbookingPercent(incoming.getOverbookingPercent());
        return repository.save(config);
    }
}
