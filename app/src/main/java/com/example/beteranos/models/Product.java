package com.example.beteranos.models;

public class Product {
    private int id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private byte[] imageBytes; // For the BLOB image

    public Product(int id, String name, String description, double price, int stock, byte[] imageBytes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageBytes = imageBytes;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public byte[] getImageBytes() { return imageBytes; }
}