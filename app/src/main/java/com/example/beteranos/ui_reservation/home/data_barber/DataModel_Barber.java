package com.example.beteranos.ui_reservation.home.data_barber;

public class DataModel_Barber {

    // 1. Private fields to hold the data for one barber
    private String name;
    private String description;
    private int imageResourceId; // Using int to store the R.drawable.id

    // 2. Constructor to initialize the fields when a new object is created
    public DataModel_Barber(String name, String description, int imageResourceId) {
        this.name = name;
        this.description = description;
        this.imageResourceId = imageResourceId;
    }

    // 3. Getter methods to allow other classes (like the Adapter) to read the data
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
}