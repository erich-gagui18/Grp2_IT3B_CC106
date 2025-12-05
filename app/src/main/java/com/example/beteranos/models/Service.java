package com.example.beteranos.models;

import java.io.Serializable;
import java.util.Objects;

public class Service implements Serializable {

    private final int serviceId;
    private final String serviceName;
    private final double price;
    private boolean isActive; // ⭐️ NEW FIELD: Not final so we can toggle it

    // ⭐️ UPDATED: Primary Constructor (Includes isActive)
    public Service(int serviceId, String serviceName, double price, boolean isActive) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.price = price;
        this.isActive = isActive;
    }

    // ⭐️ COMPATIBILITY Constructor: Defaults isActive to true
    // This prevents errors in existing code that uses the old 3-parameter constructor
    public Service(int serviceId, String serviceName, double price) {
        this(serviceId, serviceName, price, true);
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

    // ⭐️ NEW Getter and Setter
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return serviceId == service.serviceId &&
                Double.compare(service.price, price) == 0 &&
                isActive == service.isActive && // ⭐️ Added check
                Objects.equals(serviceName, service.serviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, serviceName, price, isActive); // ⭐️ Added hash
    }
}