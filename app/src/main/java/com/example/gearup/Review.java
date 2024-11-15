package com.example.gearup;

public class Review {
    private String reviewText;
    private String userId;

    public Review() {
        // Default constructor required for calls to DataSnapshot.getValue(Review.class)
    }

    public Review(String reviewText, String userId) {
        this.reviewText = reviewText;
        this.userId = userId;
    }

    public String getReviewText() {
        return reviewText;
    }

    public String getUserId() {
        return userId;
    }
}
