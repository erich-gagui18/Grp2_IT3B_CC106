package com.example.beteranos.models;

import java.util.Arrays;
import java.util.Objects;

public class Promo {
    private final int promoId;
    private final String promoName;
    private final String description;
    private final int discountPercentage;
    private final byte[] image;
    private boolean isActive; // ⭐️ NEW: Not final so we can toggle it locally if needed

    // ⭐️ UPDATED CONSTRUCTOR: Added boolean isActive
    public Promo(int promoId, String promoName, String description, int discountPercentage, byte[] image, boolean isActive) {
        this.promoId = promoId;
        this.promoName = promoName;
        this.description = description;
        this.discountPercentage = discountPercentage;
        this.image = image;
        this.isActive = isActive; // ⭐️ Assign the new field
    }

    // --- Getters ---
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

    public byte[] getImage() {
        return image;
    }

    // ⭐️ NEW Getter and Setter for Visibility
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Promo promo = (Promo) o;
        return promoId == promo.promoId &&
                discountPercentage == promo.discountPercentage &&
                isActive == promo.isActive && // ⭐️ Added check
                Objects.equals(promoName, promo.promoName) &&
                Objects.equals(description, promo.description) &&
                Arrays.equals(image, promo.image);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(promoId, promoName, description, discountPercentage, isActive); // ⭐️ Added hash
        result = 31 * result + Arrays.hashCode(image);
        return result;
    }
}