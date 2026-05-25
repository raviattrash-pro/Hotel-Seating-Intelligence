package com.hotel.seating.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
public class DiningTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String label;

    @Min(1)
    private int capacity;

    @Enumerated(EnumType.STRING)
    private SeatingZone zone;

    @Enumerated(EnumType.STRING)
    private TableStatus status = TableStatus.AVAILABLE;

    @ManyToOne
    private FloorPlan floorPlan;

    @Min(0)
    @Max(100)
    @JsonProperty("xPercent")
    @JsonAlias("xpercent")
    private int xPercent;

    @Min(0)
    @Max(100)
    @JsonProperty("yPercent")
    @JsonAlias("ypercent")
    private int yPercent;

    @Min(34)
    @Max(140)
    private Integer displayWidth = 58;

    @Min(34)
    @Max(120)
    private Integer displayHeight = 48;

    private boolean combinable;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public SeatingZone getZone() {
        return zone;
    }

    public void setZone(SeatingZone zone) {
        this.zone = zone;
    }

    public TableStatus getStatus() {
        return status;
    }

    public void setStatus(TableStatus status) {
        this.status = status;
    }

    public FloorPlan getFloorPlan() {
        return floorPlan;
    }

    public void setFloorPlan(FloorPlan floorPlan) {
        this.floorPlan = floorPlan;
    }

    public int getXPercent() {
        return xPercent;
    }

    public void setXPercent(int xPercent) {
        this.xPercent = xPercent;
    }

    public int getYPercent() {
        return yPercent;
    }

    public void setYPercent(int yPercent) {
        this.yPercent = yPercent;
    }

    public Integer getDisplayWidth() {
        return displayWidth == null ? 58 : displayWidth;
    }

    public void setDisplayWidth(Integer displayWidth) {
        this.displayWidth = displayWidth;
    }

    public Integer getDisplayHeight() {
        return displayHeight == null ? 48 : displayHeight;
    }

    public void setDisplayHeight(Integer displayHeight) {
        this.displayHeight = displayHeight;
    }

    public boolean isCombinable() {
        return combinable;
    }

    public void setCombinable(boolean combinable) {
        this.combinable = combinable;
    }
}
