package com.example.beteranos.models;

import java.util.Objects;

public class Service {
    // Assuming these are your existing fields
    private final int serviceId;
    private final String serviceName;
    private final double price;
    private final String imageName;
    private final int duration; // e.g., in minutes

    public Service(int serviceId, String serviceName, double price, String imageName, int duration) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.price = price;
        this.imageName = imageName;
        this.duration = duration;
    }

    // --- Add an overloaded constructor for the Admin panel (which only edits name/price) ---
    public Service(int serviceId, String serviceName, double price) {
        this(serviceId, serviceName, price, null, 0);
    }

    public int getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public double getPrice() {
        return price;
    }

    public String getImageName() {
        return imageName;
    }

    public int getDuration() {
        return duration;
    }

    // --- ADDED for ListAdapter/DiffUtil ---
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