package com.example.beteranos.models;

import java.util.Arrays;
import java.util.Objects;

public class Promo {
    private final int promoId;
    private final String promoName;
    private final String description;
    private final int discountPercentage; // ✅ Correct field name and type
    private final byte[] image;

    public Promo(int promoId, String promoName, String description, int discountPercentage, byte[] image) {
        this.promoId = promoId;
        this.promoName = promoName;
        this.description = description;
        this.discountPercentage = discountPercentage; // ✅ Correct assignment
        this.image = image;
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
    // --- This is the correct getter ---
    public byte[] getImage() {
        return image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Promo promo = (Promo) o;
        return promoId == promo.promoId &&
                discountPercentage == promo.discountPercentage &&
                Objects.equals(promoName, promo.promoName) &&
                Objects.equals(description, promo.description) &&
                Arrays.equals(image, promo.image); // --- Compare as bytes
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(promoId, promoName, description, discountPercentage);
        result = 31 * result + Arrays.hashCode(image); // --- Hash as bytes
        return result;
    }
}