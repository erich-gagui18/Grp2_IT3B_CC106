package com.example.beteranos.models;

import java.util.Date;
import java.util.Objects;

public class Appointment {
    private int reservationId;
    private String customerName;
    private String serviceName;
    private String barberName;
    private Date reservationTime;
    private String status;

    public Appointment(int reservationId, String customerName, String serviceName,
                       String barberName, Date reservationTime, String status) {
        this.reservationId = reservationId;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.barberName = barberName;
        this.reservationTime = reservationTime;
        this.status = status;
    }

    public int getReservationId() { return reservationId; }
    public String getCustomerName() { return customerName; }
    public String getServiceName() { return serviceName; }
    public String getBarberName() { return barberName; }
    public Date getReservationTime() { return reservationTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Appointment)) return false;
        Appointment that = (Appointment) o;
        return reservationId == that.reservationId &&
                Objects.equals(customerName, that.customerName) &&
                Objects.equals(serviceName, that.serviceName) &&
                Objects.equals(barberName, that.barberName) &&
                Objects.equals(reservationTime, that.reservationTime) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId, customerName, serviceName, barberName, reservationTime, status);
    }
}
