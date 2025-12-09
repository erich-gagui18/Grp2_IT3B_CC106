package com.example.beteranos.models;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;

public class Review {
    private final int reviewId;
    private final String customerName; // e.g., "John D."
    private final String barberName;
    private final int rating;
    private final String comment;
    private final Timestamp createdAt;

    // ⭐️ NEW FIELD: For storing the image bytes
    private final byte[] reviewImage;

    // ⭐️ UPDATED CONSTRUCTOR: Accepts 7 parameters (Matches your ViewModel)
    public Review(int reviewId, String customerName, String barberName, int rating, String comment, Timestamp createdAt, byte[] reviewImage) {
        this.reviewId = reviewId;
        this.customerName = customerName;
        this.barberName = barberName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.reviewImage = reviewImage;
    }

    // ⭐️ COMPATIBILITY CONSTRUCTOR: 6 parameters (Defaults image to null)
    // This prevents crashes in other parts of the app that might still use the old format
    public Review(int reviewId, String customerName, String barberName, int rating, String comment, Timestamp createdAt) {
        this(reviewId, customerName, barberName, rating, comment, createdAt, null);
    }

    // Getters
    public int getReviewId() { return reviewId; }
    public String getCustomerName() { return customerName; }
    public String getBarberName() { return barberName; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public Timestamp getCreatedAt() { return createdAt; }

    // ⭐️ NEW GETTER
    public byte[] getReviewImage() { return reviewImage; }

    // --- Added for ListAdapter/DiffUtil ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return reviewId == review.reviewId &&
                rating == review.rating &&
                Objects.equals(comment, review.comment) &&
                Arrays.equals(reviewImage, review.reviewImage); // ⭐️ Deep check for image bytes
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(reviewId, rating, comment);
        result = 31 * result + Arrays.hashCode(reviewImage);
        return result;
    }
}