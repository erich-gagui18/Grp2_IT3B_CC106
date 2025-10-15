package com.example.beteranos.models;

public class Barber {
    private final int id;
    private final String name;

    public Barber(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}