package com.example.beteranos.models;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.Locale;

public class Transaction {
    private final int reservationId;
    private final Timestamp reservationTime;
    private final String customerName;
    private final String barberName;
    private final String services;

    // ⭐️ Updated Financial Fields ⭐️
    private final double totalPrice;        // Total services price (pre-discount)
    private final double discountAmount;    // Total discount applied
    private final double finalPrice;        // Total amount due after discount (Total transaction value)
    private final double downPaymentAmount; // Amount paid at booking (50% payment)

    // ⭐️ Updated Constructor ⭐️
    public Transaction(int reservationId, Timestamp reservationTime, String customerName,
                       String barberName, String services, double finalPrice,
                       double downPaymentAmount, double totalPrice, double discountAmount) {
        this.reservationId = reservationId;
        this.reservationTime = reservationTime;
        this.customerName = customerName;
        this.barberName = barberName;
        this.services = services;
        this.finalPrice = finalPrice;
        this.downPaymentAmount = downPaymentAmount;
        this.totalPrice = totalPrice;
        this.discountAmount = discountAmount;
    }

    // --- Getters (Updated/New) ---
    public int getReservationId() { return reservationId; }
    public Timestamp getReservationTime() { return reservationTime; }
    public String getCustomerName() { return customerName; }
    public String getBarberName() { return barberName; }
    public String getServices() { return services; }

    // Note: The previous getTotalAmount() is now getFinalPrice() for clarity.
    public double getFinalPrice() { return finalPrice; }
    public double getDownPaymentAmount() { return downPaymentAmount; }
    public double getTotalPrice() { return totalPrice; }
    public double getDiscountAmount() { return discountAmount; }

    // --- Calculated Field for Reporting ---

    /**
     * Calculates the remaining balance the customer owes upon service completion.
     * Calculated as: Final Price - Down Payment Paid.
     * @return The remaining amount due.
     */
    public double getRemainingBalance() {
        return finalPrice - downPaymentAmount;
    }

    // --- Utility Getter for UI Formatting ---
    public String getFormattedRemainingBalance() {
        return String.format(Locale.US, "₱%.2f", getRemainingBalance());
    }

    public String getFormattedFinalPrice() {
        return String.format(Locale.US, "₱%.2f", finalPrice);
    }

    // --- Standard Methods (Unchanged) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return reservationId == that.reservationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }
}