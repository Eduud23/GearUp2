package com.example.gearup;

public class NotificationModel {
    private String title;
    private String body;
    private long timestamp;

    public NotificationModel(String title, String body) {
        this.title = title;
        this.body = body;
        this.timestamp = this.timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;

    }

    public long getTimestamp() {
        return timestamp;
    }
}