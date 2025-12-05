package com.example.beteranos.models;

import java.io.Serializable;
import java.util.Objects;

public class Barber implements Serializable {

    private final int barberId;
    private final String name;
    private final String specialization;

    // ⭐️ NEW FIELDS ⭐️
    private final int experienceYears;
    private final String contactNumber;

    private final String imageUrl;
    private final String dayOff;

    // ⭐️ UPDATED CONSTRUCTOR (Matches the 7 parameters in your ViewModel) ⭐️
    public Barber(int barberId, String name, String specialization, int experienceYears, String contactNumber, String imageUrl, String dayOff) {
        this.barberId = barberId;
        this.name = name;
        this.specialization = specialization;
        this.experienceYears = experienceYears;
        this.contactNumber = contactNumber;
        this.imageUrl = imageUrl;
        this.dayOff = dayOff;
    }

    // --- Getters ---
    public int getBarberId() {
        return barberId;
    }

    public String getName() {
        return name;
    }

    public String getSpecialization() {
        return specialization;
    }

    // ⭐️ NEW GETTERS ⭐️
    public int getExperienceYears() {
        return experienceYears;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDayOff() {
        return dayOff;
    }

    // --- Comparisons ---
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

    @Override
    public String toString() {
        return name; // Helpful for debugging
    }
}