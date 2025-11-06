package com.example.beteranos.models;

import java.util.Objects;

public class Promo {
    private final int promoId;
    private final String promoName;
    private final String description;
    private final int discountPercentage;
    private final String imageName; // Added from your old model

    // Main constructor with all fields
    public Promo(int promoId, String promoName, String description, int discountPercentage, String imageName) {
        this.promoId = promoId;
        this.promoName = promoName;
        this.description = description;
        this.discountPercentage = discountPercentage;
        this.imageName = imageName;
    }

    // Overloaded constructor for new Admin Management (doesn't use image_name)
    public Promo(int promoId, String promoName, String description, int discountPercentage) {
        this(promoId, promoName, description, discountPercentage, null);
    }

    // Overloaded constructor for old Reservation Flow (doesn't use new fields)
    public Promo(int promoId, String promoName, String imageName) {
        this(promoId, promoName, null, 0, imageName);
    }

    public int getPromoId() {
        return promoId;
    }

    public String getPromoName() {
        return promoName;
    }

    public String getDescription() {
        return description;
    }

    public int getDiscountPercentage() {
        return discountPercentage;
    }

    public String getImageName() {
        return imageName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Promo promo = (Promo) o;
        return promoId == promo.promoId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(promoId);
    }
}