package com.example.gearup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Notification {
    private String message;
    private String buyerId;
    private String orderId;
    private long timestamp;
    private String sellerId;
    private String receiverId;  // Add receiverId for filtering by recipient

    // Default constructor required for Firestore
    public Notification() {}

    public Notification(String message, String buyerId, String orderId, long timestamp, String sellerId, String receiverId) {
        this.message = message;
        this.buyerId = buyerId;
        this.orderId = orderId;
        this.timestamp = timestamp;
        this.sellerId = sellerId;
        this.receiverId = receiverId;  // Initialize receiverId
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

    public String getReceiverId() {  // Getter for receiverId
        return receiverId;
    }

    public void setReceiverId(String receiverId) {  // Setter for receiverId
        this.receiverId = receiverId;
    }

    // Method to format the timestamp into a readable string
    public String getFormattedTimestamp() {
        Date date = new Date(timestamp);  // Convert timestamp (in milliseconds) to Date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(date);  // Return the formatted date as a string
    }
}
