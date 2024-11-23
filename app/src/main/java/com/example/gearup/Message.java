package com.example.gearup;

public class Message {
    private String senderId;
    private String receiverId;
    private String content;
    private long timestamp;
    private String status; // To track if the message is read or unread

    // No-argument constructor (required for Firestore)
    public Message() {
        // Empty constructor required for Firestore deserialization
    }

    // Constructor with parameters (used for creating a new message)
    public Message(String senderId, String receiverId, String content, long timestamp, String status) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Getters and setters
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
