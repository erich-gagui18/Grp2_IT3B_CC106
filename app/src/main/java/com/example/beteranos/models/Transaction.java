package com.example.beteranos.models;

import java.sql.Timestamp;
import java.util.Objects;

public class Transaction {
    private final int reservationId;
    private final Timestamp reservationTime;
    private final String customerName;
    private final String barberName;
    private final String services;
    private final double totalAmount;

    public Transaction(int reservationId, Timestamp reservationTime, String customerName, String barberName, String services, double totalAmount) {
        this.reservationId = reservationId;
        this.reservationTime = reservationTime;
        this.customerName = customerName;
        this.barberName = barberName;
        this.services = services;
        this.totalAmount = totalAmount;
    }

    public int getReservationId() { return reservationId; }
    public Timestamp getReservationTime() { return reservationTime; }
    public String getCustomerName() { return customerName; }
    public String getBarberName() { return barberName; }
    public String getServices() { return services; }
    public double getTotalAmount() { return totalAmount; }

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