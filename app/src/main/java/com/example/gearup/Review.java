package com.example.gearup;

public class Review {
    private String reviewText; // Review content
    private String userId;      // ID of the user who submitted the review
    private long timestamp;      // Optional: Timestamp for when the review was created

    // Default constructor (required for Firestore)
    public Review() {}

    // Constructor
    public Review(String reviewText, String userId) {
        this.reviewText = reviewText;
        this.userId = userId;
        this.timestamp = System.currentTimeMillis(); // Set current time
    }

    public String getReviewText() {
        return reviewText;
    }

    public String getUserId() {
        return userId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
