package com.group06.music_app_mobile.app_utils;

import android.os.Build;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppUtils {

    private AppUtils() {}

    public static String getTimeAgo(LocalDateTime createdDate) {
        if (createdDate == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdDate, now);

        long seconds = duration.getSeconds();
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (seconds < 5) {
            return "Vừa xong";
        } else if (seconds < 60) {
            return seconds + " seconds ago";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else if (days == 1) {
            return "Hôm qua";
        } else if (days < 7) {
            return days + " days ago";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + " weeks ago";
        } else if (days < 365) {
            long months = days / 30;
            return months + " months ago";
        } else {
            long years = days / 365;
            return years + " years ago";
        }
    }

    public static String localDateTimeToString(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return dateTime.format(formatter);
    }
}
