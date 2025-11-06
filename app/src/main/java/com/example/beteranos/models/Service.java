package com.example.beteranos.models;

import java.util.Objects;

public class Service {

    // --- UPDATED: These fields now match your DB schema ---
    private final int serviceId;
    private final String serviceName;
    private final double price;
    // private final String imageName;  // --- REMOVED ---
    // private final int duration; // --- REMOVED ---

    // --- UPDATED: This is now the main constructor ---
    public Service(int serviceId, String serviceName, double price) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.price = price;
    }

    // --- REMOVED the old constructors ---

    public int getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public double getPrice() {
        return price;
    }

    // --- REMOVED getters for imageName and duration ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return serviceId == service.serviceId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId);
    }
}