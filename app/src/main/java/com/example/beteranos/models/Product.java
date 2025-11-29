package com.example.beteranos.models;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class Product {
    private int id;
    private String name;
    private String description;
    private double price;
    private int stock; // Java field name
    private byte[] imageBytes;

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

    public String getFormattedPrice() {
        return String.format(Locale.US, "₱%.2f", price);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id &&
                Double.compare(product.price, price) == 0 &&
                stock == product.stock &&
                Objects.equals(name, product.name) &&
                Objects.equals(description, product.description) &&
                Arrays.equals(imageBytes, product.imageBytes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, name, description, price, stock);
        result = 31 * result + Arrays.hashCode(imageBytes);
        return result;
    }
}