package com.example.beteranos.ui_reservation.home.data_barber;

public class DataModel_Barber {

    // 1. Private fields to hold the data for one barber
    private String name;
    private String description;
    private int imageResourceId;
    private float rating; // ⭐️ NEW: Field for storing the rating ⭐️

    // 2. Constructor (UPDATED to include rating)
    public DataModel_Barber(String name, String description, int imageResourceId, float rating) {
        this.name = name;
        this.description = description;
        this.imageResourceId = imageResourceId;
        this.rating = rating; // Initialize the new field
    }

    // 3. Getter methods
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    // ⭐️ NEW: Getter for the rating ⭐️
    public float getRating() {
        return rating;
    }
}