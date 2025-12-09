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
    private boolean isActive;

    // ⭐️ NEW FIELDS: For Schedule
    private final String startTime;
    private final String endTime;

    // ⭐️ 1. NEW PRIMARY CONSTRUCTOR (10 Params)
    // Used by AdminManagementBarbersViewModel to include Schedule Times
    public Barber(int barberId, String name, String specialization, int experienceYears,
                  String contactNumber, String imageUrl, String dayOff, boolean isActive,
                  String startTime, String endTime) {
        this.barberId = barberId;
        this.name = name;
        this.specialization = specialization;
        this.experienceYears = experienceYears;
        this.contactNumber = contactNumber;
        this.imageUrl = imageUrl;
        this.dayOff = dayOff;
        this.isActive = isActive;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // ⭐️ 2. COMPATIBILITY CONSTRUCTOR (8 Params)
    // Used by parts of the app that haven't implemented Time yet (Defaults to null)
    public Barber(int barberId, String name, String specialization, int experienceYears,
                  String contactNumber, String imageUrl, String dayOff, boolean isActive) {
        this(barberId, name, specialization, experienceYears, contactNumber, imageUrl, dayOff, isActive, null, null);
    }

    // ⭐️ 3. COMPATIBILITY CONSTRUCTOR (7 Params)
    // Defaults isActive to true, and times to null
    public Barber(int barberId, String name, String specialization, int experienceYears,
                  String contactNumber, String imageUrl, String dayOff) {
        this(barberId, name, specialization, experienceYears, contactNumber, imageUrl, dayOff, true, null, null);
    }

    // ⭐️ 4. LEGACY CONSTRUCTOR (5 Params)
    // Defaults extra fields to dummy values
    public Barber(int barberId, String name, String specialization, String dayOff, String imageUrl) {
        this(barberId, name, specialization, 0, "N/A", imageUrl, dayOff, true, null, null);
    }

    // --- Getters ---
    public int getBarberId() { return barberId; }
    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public int getExperienceYears() { return experienceYears; }
    public String getContactNumber() { return contactNumber; }
    public String getImageUrl() { return imageUrl; }
    public String getDayOff() { return dayOff; }
    public boolean isActive() { return isActive; }

    // ⭐️ NEW GETTERS
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }

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
                isActive == barber.isActive &&
                Objects.equals(startTime, barber.startTime) && // ⭐️ Check time
                Objects.equals(endTime, barber.endTime);       // ⭐️ Check time
    }

    @Override
    public int hashCode() {
        return Objects.hash(barberId, isActive, startTime, endTime);
    }

    @Override
    public String toString() {
        return name;
    }
}