package com.example.gearup;

public class Review {
    private String reviewText;
    private String userId;
    private String userName; // Buyer name or shop name (seller name)
    private String profileImageUrl;

    // No-argument constructor required by Firestore for deserialization
    public Review() {
        // Firestore needs an empty constructor
    }

    // Custom constructor with parameters
    public Review(String reviewText, String userId, String userName, String profileImageUrl) {
        this.reviewText = reviewText;
        this.userId = userId;
        this.userName = userName;
        this.profileImageUrl = profileImageUrl;
    }

    // Getters and setters (if needed)
    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
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
}
