package com.example.gearup;

import com.google.firebase.Timestamp;

public class Review {
    private String reviewText;
    private double starRating; // Change from String to float
    private String userId;
    private String userName;
    private String profileImageUrl;
    private Timestamp timestamp;

    // No-argument constructor required by Firestore
    public Review() {
    }

    // Constructor with star rating
    public Review(String reviewText, double starRating, String userId, String userName, String profileImageUrl, Timestamp timestamp) {
        this.reviewText = reviewText;
        this.starRating = starRating;
        this.userId = userId;
        this.userName = userName;
        this.profileImageUrl = profileImageUrl;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public double getStarRating() {  // Change return type to float
        return starRating;
    }

    public void setStarRating(float starRating) {
        this.starRating = starRating;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setPTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
