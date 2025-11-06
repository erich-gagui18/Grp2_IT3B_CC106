package com.example.beteranos.models;

import java.util.Objects;

public class Promo {
    private final int promoId;
    private final String promoName;
    private final String description;
    private final int discountPercentage;
    private final String imageName;

    // Main constructor (from before)
    public Promo(int promoId, String promoName, String description, int discountPercentage, String imageName) {
        this.promoId = promoId;
        this.promoName = promoName;
        this.description = description;
        this.discountPercentage = discountPercentage;
        this.imageName = imageName;
    }

    // Admin constructor (from before)
    public Promo(int promoId, String promoName, String description, int discountPercentage) {
        this(promoId, promoName, description, discountPercentage, null);
    }

    // Old Reservation constructor (THIS WAS THE PROBLEM)
    public Promo(int promoId, String promoName, String imageName) {
        this(promoId, promoName, null, 0, imageName); // It was setting description to null
    }

    // --- ADD THIS NEW CONSTRUCTOR FOR THE FIX ---
    public Promo(int promoId, String promoName, String description, String imageName) {
        this(promoId, promoName, description, 0, imageName); // Calls the main constructor
    }

    public int getPromoId() {
        return promoId;
    }

    // ... (rest of your getters and equals/hashCode methods) ...
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