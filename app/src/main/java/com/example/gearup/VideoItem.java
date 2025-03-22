package com.example.gearup;

public class VideoItem {
    private String title;
    private String thumbnail;
    private String url;

    public VideoItem(String title, String thumbnail, String url) {
        this.title = title;
        this.thumbnail = thumbnail;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getUrl() {
        return url;
    }
}