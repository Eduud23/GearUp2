package com.example.gearup;

public class Comment {
    private String commentText;
    private String userId;
    private long timestamp;

    public Comment() {
        // Empty constructor needed for Firestore
    }

    public Comment(String commentText, String userId) {
        this.commentText = commentText;
        this.userId = userId;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters
    public String getCommentText() {
        return commentText;
    }

    public String getUserId() {
        return userId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
