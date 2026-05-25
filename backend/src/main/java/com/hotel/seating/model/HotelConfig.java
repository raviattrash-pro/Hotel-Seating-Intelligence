package com.hotel.seating.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
public class HotelConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String hotelName;

    @Lob
    private String floorPlanImageUrl;

    private String upiId = "hotel@upi";

    @Lob
    private String qrCodeImageUrl;

    @Min(0)
    private int bookingDepositInr = 50;

    @Min(1)
    private int slotMinutes = 90;

    @Min(0)
    private int overbookingPercent = 10;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getFloorPlanImageUrl() {
        return floorPlanImageUrl;
    }

    public void setFloorPlanImageUrl(String floorPlanImageUrl) {
        this.floorPlanImageUrl = floorPlanImageUrl;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public String getQrCodeImageUrl() {
        return qrCodeImageUrl;
    }

    public void setQrCodeImageUrl(String qrCodeImageUrl) {
        this.qrCodeImageUrl = qrCodeImageUrl;
    }

    public int getBookingDepositInr() {
        return bookingDepositInr;
    }

    public void setBookingDepositInr(int bookingDepositInr) {
        this.bookingDepositInr = bookingDepositInr;
    }

    public int getSlotMinutes() {
        return slotMinutes;
    }

    public void setSlotMinutes(int slotMinutes) {
        this.slotMinutes = slotMinutes;
    }

    public int getOverbookingPercent() {
        return overbookingPercent;
    }

    public void setOverbookingPercent(int overbookingPercent) {
        this.overbookingPercent = overbookingPercent;
    }
}
