package com.example.beteranos.models;

import java.util.Objects; // Import this

public class Barber {
    // Renamed 'id' to 'barberId' for better clarity
    private final int barberId;
    private final String name;

    // --- ADDED ---
    private final String specialization;

    // --- UPDATED CONSTRUCTOR ---
    public Barber(int barberId, String name, String specialization) {
        this.barberId = barberId;
        this.name = name;
        this.specialization = specialization;
    }

    // --- RENAMED GETTER ---
    public int getBarberId() {
        return barberId;
    }

    public String getName() {
        return name;
    }

    // --- ADDED GETTER ---
    public String getSpecialization() {
        return specialization;
    }

    // --- ADDED for ListAdapter/DiffUtil ---
    // This allows the adapter to efficiently check if two Barber objects are the same
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Barber barber = (Barber) o;
        return barberId == barber.barberId; // Barbers are the "same item" if their ID is the same
    }

    // --- ADDED for ListAdapter/DiffUtil ---
    @Override
    public int hashCode() {
        return Objects.hash(barberId);
    }
}