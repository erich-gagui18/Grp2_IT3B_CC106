package com.example.beteranos.models;

import java.io.Serializable;
import java.util.Objects;

public class Barber implements Serializable {

    private final int barberId;
    private final String name;
    private final String specialization;
    private final int experienceYears;
    private final String contactNumber;
    private final String imageUrl;
    private final String dayOff;
    private boolean isActive; // ⭐️ NEW FIELD (Not final to allow toggling)

    // ⭐️ 1. PRIMARY CONSTRUCTOR (8 Params - Used by Admin ViewModel)
    public Barber(int barberId, String name, String specialization, int experienceYears, String contactNumber, String imageUrl, String dayOff, boolean isActive) {
        this.barberId = barberId;
        this.name = name;
        this.specialization = specialization;
        this.experienceYears = experienceYears;
        this.contactNumber = contactNumber;
        this.imageUrl = imageUrl;
        this.dayOff = dayOff;
        this.isActive = isActive;
    }

    // ⭐️ 2. COMPATIBILITY CONSTRUCTOR (7 Params - Defaults isActive to true)
    // Used by ViewModels that haven't been updated for visibility logic yet
    public Barber(int barberId, String name, String specialization, int experienceYears, String contactNumber, String imageUrl, String dayOff) {
        this(barberId, name, specialization, experienceYears, contactNumber, imageUrl, dayOff, true);
    }

    // ⭐️ 3. LEGACY CONSTRUCTOR (5 Params - Defaults exp, contact, and isActive)
    // Used by older parts of the app to prevent build errors
    public Barber(int barberId, String name, String specialization, String dayOff, String imageUrl) {
        this(barberId, name, specialization, 0, "N/A", imageUrl, dayOff, true);
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

    // ⭐️ NEW Getter and Setter
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // --- Comparisons ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Barber barber = (Barber) o;
        return barberId == barber.barberId &&
                isActive == barber.isActive; // ⭐️ Include in check
    }

    @Override
    public int hashCode() {
        return Objects.hash(barberId, isActive); // ⭐️ Include in hash
    }

    @Override
    public String toString() {
        return name;
    }
}