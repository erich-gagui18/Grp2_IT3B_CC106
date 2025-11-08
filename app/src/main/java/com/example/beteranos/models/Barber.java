package com.example.beteranos.models;

import java.util.Objects;

public class Barber {

    private final int barberId;
    private final String name;
    private final String specialization;
    private final String dayOff;
    // 🔑 ADDED: Field for the barber's profile image URL
    private final String imageUrl;

    // 🔑 UPDATED CONSTRUCTOR: Now includes dayOff and imageUrl
    public Barber(int barberId, String name, String specialization, String dayOff, String imageUrl) {
        this.barberId = barberId;
        this.name = name;
        this.specialization = specialization;
        this.dayOff = dayOff;
        this.imageUrl = imageUrl; // Assign the new field
    }

    public int getBarberId() {
        return barberId;
    }

    public String getName() {
        return name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getDayOff() {
        return dayOff;
    }

    // 🔑 ADDED GETTER: For Image URL
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Barber barber = (Barber) o;
        return barberId == barber.barberId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(barberId);
    }
}