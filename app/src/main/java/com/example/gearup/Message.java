package com.example.gearup;

public class Message {
    private String senderId;
    private String content;
    private long timestamp;

    // No-argument constructor is required for Firestore to deserialize the object
    public Message() {
        // Firestore requires a no-argument constructor
    }

    public Message(String senderId, String content, long timestamp) {
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getter and setter methods

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // A helper method to determine if the current message is from the current user
    public boolean isSentByCurrentUser(String currentUserId) {
        return senderId.equals(currentUserId);
    }
}
