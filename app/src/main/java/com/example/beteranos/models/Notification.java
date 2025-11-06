package com.example.beteranos.models;

import java.sql.Timestamp;
import java.util.Objects;

public class Notification {
    private final int notificationId;
    private final String title;
    private final String body;
    private final Timestamp createdAt;
    private boolean isRead;

    public Notification(int notificationId, String title, String body, Timestamp createdAt, boolean isRead) {
        this.notificationId = notificationId;
        this.title = title;
        this.body = body;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    // Getters
    public int getNotificationId() { return notificationId; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public Timestamp getCreatedAt() { return createdAt; }
    public boolean isRead() { return isRead; }

    // --- Needed for ListAdapter/DiffUtil ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return notificationId == that.notificationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId);
    }
}