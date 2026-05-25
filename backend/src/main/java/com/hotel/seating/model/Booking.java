package com.hotel.seating.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String guestName;

    @NotBlank
    private String phone;

    @Min(1)
    private int partySize;

    private LocalDate bookingDate;

    private LocalTime bookingTime;

    @Enumerated(EnumType.STRING)
    private SeatingZone preferredZone;

    @Enumerated(EnumType.STRING)
    private BookingSource source;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.RESERVED;

    @ManyToOne
    private DiningTable diningTable;

    private int depositAmountInr = 50;

    private boolean depositRefunded;

    private String paymentTransactionId;

    @Lob
    private String paymentScreenshotUrl;

    private boolean paymentVerified;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime checkedInAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getPartySize() {
        return partySize;
    }

    public void setPartySize(int partySize) {
        this.partySize = partySize;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public SeatingZone getPreferredZone() {
        return preferredZone;
    }

    public void setPreferredZone(SeatingZone preferredZone) {
        this.preferredZone = preferredZone;
    }

    public BookingSource getSource() {
        return source;
    }

    public void setSource(BookingSource source) {
        this.source = source;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public DiningTable getDiningTable() {
        return diningTable;
    }

    public void setDiningTable(DiningTable diningTable) {
        this.diningTable = diningTable;
    }

    public int getDepositAmountInr() {
        return depositAmountInr;
    }

    public void setDepositAmountInr(int depositAmountInr) {
        this.depositAmountInr = depositAmountInr;
    }

    public boolean isDepositRefunded() {
        return depositRefunded;
    }

    public void setDepositRefunded(boolean depositRefunded) {
        this.depositRefunded = depositRefunded;
    }

    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    public String getPaymentScreenshotUrl() {
        return paymentScreenshotUrl;
    }

    public void setPaymentScreenshotUrl(String paymentScreenshotUrl) {
        this.paymentScreenshotUrl = paymentScreenshotUrl;
    }

    public boolean isPaymentVerified() {
        return paymentVerified;
    }

    public void setPaymentVerified(boolean paymentVerified) {
        this.paymentVerified = paymentVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(LocalDateTime checkedInAt) {
        this.checkedInAt = checkedInAt;
    }
}
