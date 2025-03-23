package com.example.gearup;

public class VideoItem {
    private String title;
    private String thumbnail;
    private String url;
    private String channelTitle;

    public VideoItem(String title, String thumbnail, String url, String channelTitle) {
        this.title = title;
        this.thumbnail = thumbnail;
        this.url = url;
        this.channelTitle = channelTitle;
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

    public String getChannelTitle() {
        return channelTitle;
    }
}
