package com.example.beteranos.models;

import java.sql.Timestamp;
import java.util.Objects;

public class Review {
    private final int reviewId;
    private final String customerName; // e.g., "John D."
    private final String barberName;
    private final int rating;
    private final String comment;
    private final Timestamp createdAt;

    public Review(int reviewId, String customerName, String barberName, int rating, String comment, Timestamp createdAt) {
        this.reviewId = reviewId;
        this.customerName = customerName;
        this.barberName = barberName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getters
    public int getReviewId() { return reviewId; }
    public String getCustomerName() { return customerName; }
    public String getBarberName() { return barberName; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public Timestamp getCreatedAt() { return createdAt; }

    // --- Added for ListAdapter/DiffUtil ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return reviewId == review.reviewId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewId);
    }
}