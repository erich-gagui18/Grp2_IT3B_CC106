package com.example.beteranos.models;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.Locale;

public class Transaction {

    // Core Fields (Made non-final for JDBC Setters)
    private String reservationId;
    private Timestamp reservationTime;
    private String customerName;
    private String barberName;
    private String services;

    // Financial Fields
    private double totalPrice;
    private double discountAmount;
    private double finalPrice;
    private double downPaymentAmount;

    private String status;
    private double remainingBalance; // Value is set by the Repository

    // Required for JDBC mapping
    public Transaction() {
    }

    // --- Getters ---
    public String getReservationId() { return reservationId; }
    public Timestamp getReservationTime() { return reservationTime; }
    public String getCustomerName() { return customerName; }
    public String getBarberName() { return barberName; }
    public String getServices() { return services; }
    public String getStatus() { return status; }
    public double getFinalPrice() { return finalPrice; }
    public double getDownPaymentAmount() { return downPaymentAmount; }
    public double getTotalPrice() { return totalPrice; }
    public double getDiscountAmount() { return discountAmount; }
    public double getRemainingBalance() { return remainingBalance; }

    // --- Setters (Used by TransactionRepository) ---
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }
    public void setReservationTime(Timestamp reservationTime) { this.reservationTime = reservationTime; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setBarberName(String barberName) { this.barberName = barberName; }
    public void setServices(String services) { this.services = services; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }
    public void setFinalPrice(double finalPrice) { this.finalPrice = finalPrice; }
    public void setDownPaymentAmount(double downPaymentAmount) { this.downPaymentAmount = downPaymentAmount; }
    public void setStatus(String status) { this.status = status; }
    public void setRemainingBalance(double remainingBalance) { this.remainingBalance = remainingBalance; }

    // --- Formatted Getters (Used by Adapter and Dialog) ---
    public String getFormattedRemainingBalance() {
        return String.format(Locale.US, "₱%.2f", getRemainingBalance());
    }

    public String getFormattedFinalPrice() {
        return String.format(Locale.US, "₱%.2f", finalPrice);
    }

    // For DiffUtil
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(reservationId, that.reservationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }
}