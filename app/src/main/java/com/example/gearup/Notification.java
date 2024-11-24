package com.example.gearup;

public class Notification {
    private String message;
    private String buyerId;
    private String orderId;
    private long timestamp;
    private String sellerId;

    // Default constructor required for Firestore
    public Notification() {}

    public Notification(String message, String buyerId, String orderId, long timestamp, String sellerId) {
        this.message = message;
        this.buyerId = buyerId;
        this.orderId = orderId;
        this.timestamp = timestamp;
        this.sellerId = sellerId;
    }

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }
}
