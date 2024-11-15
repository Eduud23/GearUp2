package com.example.gearup;

public class Chatroom {

    private String chatId;
    private String lastMessageSenderId;
    private long lastMessageTimestamp;
    private String currentUserId;
    private String sellerId;

    // Empty constructor for Firestore
    public Chatroom() {}

    // Constructor
    public Chatroom(String chatId, String lastMessageSenderId, long lastMessageTimestamp, String currentUserId, String sellerId) {
        this.chatId = chatId;
        this.lastMessageSenderId = lastMessageSenderId;
        this.lastMessageTimestamp = lastMessageTimestamp;
        this.currentUserId = currentUserId;
        this.sellerId = sellerId;
    }

    // Getters and setters
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getLastMessageSenderId() {
        return lastMessageSenderId;
    }

    public void setLastMessageSenderId(String lastMessageSenderId) {
        this.lastMessageSenderId = lastMessageSenderId;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }
}

