package com.example.beteranos.models;

import java.sql.Timestamp;
import java.util.Arrays; // ⭐️ ADD THIS IMPORT
import java.util.Objects;
import java.util.Locale;

public class Transaction {

    // Core Fields
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
    private double remainingBalance;

    // --- ⭐️ THIS IS THE FIX ⭐️ ---
    // Added the field for the payment receipt
    private byte[] paymentReceipt;
    // --- END OF FIX ---

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

    // --- ⭐️ ADD GETTER FOR THE FIX ⭐️ ---
    // Name matches your Appointment.java model for consistency
    public byte[] getPaymentReceiptBytes() { return paymentReceipt; }

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

    // --- ⭐️ ADD SETTER FOR THE FIX ⭐️ ---
    public void setPaymentReceiptBytes(byte[] paymentReceipt) { this.paymentReceipt = paymentReceipt; }

    // --- Formatted Getters (Used by Adapter and Dialog) ---
    public String getFormattedRemainingBalance() {
        // Added safety check: if 'Completed', remaining balance is always 0
        if ("Completed".equalsIgnoreCase(status)) {
            return String.format(Locale.US, "₱%.2f", 0.0);
        }
        return String.format(Locale.US, "₱%.2f", getRemainingBalance());
    }

    public String getFormattedFinalPrice() {
        return String.format(Locale.US, "₱%.2f", finalPrice);
    }

    // --- ⭐️ ROBUST equals() & hashCode() for ListAdapter ⭐️ ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Double.compare(that.totalPrice, totalPrice) == 0 &&
                Double.compare(that.discountAmount, discountAmount) == 0 &&
                Double.compare(that.finalPrice, finalPrice) == 0 &&
                Double.compare(that.downPaymentAmount, downPaymentAmount) == 0 &&
                Double.compare(that.remainingBalance, remainingBalance) == 0 &&
                Objects.equals(reservationId, that.reservationId) &&
                Objects.equals(reservationTime, that.reservationTime) &&
                Objects.equals(customerName, that.customerName) &&
                Objects.equals(barberName, that.barberName) &&
                Objects.equals(services, that.services) &&
                Objects.equals(status, that.status) &&
                Arrays.equals(paymentReceipt, that.paymentReceipt); // <-- Compare bytes
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(reservationId, reservationTime, customerName, barberName, services, totalPrice, discountAmount, finalPrice, downPaymentAmount, status, remainingBalance);
        result = 31 * result + Arrays.hashCode(paymentReceipt); // <-- Hash bytes
        return result;
    }
}