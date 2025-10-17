package com.example.beteranos.models;

import java.sql.Timestamp;

public class Appointment {
    private final int reservationId;
    private final String customerName;
    private final String serviceName;
    private final String barberName;
    private final Timestamp reservationTime;
    private final String status;

    public Appointment(int reservationId, String customerName, String serviceName, String barberName, Timestamp reservationTime, String status) {
        this.reservationId = reservationId;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.barberName = barberName;
        this.reservationTime = reservationTime;
        this.status = status;
    }

    // Getters for all fields
    public int getReservationId() { return reservationId; }
    public String getCustomerName() { return customerName; }
    public String getServiceName() { return serviceName; }
    public String getBarberName() { return barberName; }
    public Timestamp getReservationTime() { return reservationTime; }
    public String getStatus() { return status; }
}