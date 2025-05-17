package com.group06.music_app_mobile.app_utils;

import android.os.Build;

import java.time.Duration;
import java.time.LocalDateTime;

public class AppUtils {

    private AppUtils() {}

    public static String getTimeAgo(LocalDateTime createdDate) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || createdDate == null) {
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
            return seconds + " giây trước";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (hours < 24) {
            return hours + " giờ trước";
        } else if (days == 1) {
            return "Hôm qua";
        } else if (days < 7) {
            return days + " ngày trước";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + " tuần trước";
        } else if (days < 365) {
            long months = days / 30;
            return months + " tháng trước";
        } else {
            long years = days / 365;
            return years + " năm trước";
        }
    }
}
