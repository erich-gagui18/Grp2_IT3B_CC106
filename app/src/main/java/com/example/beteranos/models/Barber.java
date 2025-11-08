package com.example.beteranos.models;

import java.util.Objects;

public class Barber {

    private final int barberId;
    private final String name;

    private final String specialization;
    // 🔑 ADDED: Day Off field
    private final String dayOff;

    // 🔑 UPDATED CONSTRUCTOR: Now includes dayOff
    public Barber(int barberId, String name, String specialization, String dayOff) {
        this.barberId = barberId;
        this.name = name;
        this.specialization = specialization;
        this.dayOff = dayOff; // Assign the new field
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

    // 🔑 ADDED GETTER: For Day Off
    public String getDayOff() {
        return dayOff;
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