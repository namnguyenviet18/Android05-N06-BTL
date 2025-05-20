package com.group06.music_app_mobile.api_client.responses;

public class RecentSong {
    private String title;
    private String subtitle;
    private String duration;
    private int imageResId;

    public RecentSong(String title, String subtitle, String duration, int imageResId) {
        this.title = title;
        this.subtitle = subtitle;
        this.duration = duration;
        this.imageResId = imageResId;
    }

    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getDuration() { return duration; }
    public int getImageResId() { return imageResId; }
}

