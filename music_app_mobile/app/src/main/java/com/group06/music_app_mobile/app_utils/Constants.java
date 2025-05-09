package com.group06.music_app_mobile.app_utils;

public class Constants {

    public static final String SERVER_DOMAIN = String.format(
            "http://%s:%s/api/v1/",
            ServerDestination.SERVER_HOST,
            ServerDestination.SERVER_PORT
    );
    public static final String ACCESS_TOKEN = "access-token";
    public static final String REFRESH_TOKEN = "access-token";
    public static final String IS_FIRST_LAUNCH = "is-first-launch";
    public static final String IS_FIRST_WELCOME = "is-first-welcome";
    public static final String GOOGLE_CLIENT_ID = "1078270987220-ugprd9k9gh03ks34acc1jg5jcfddhl3h.apps.googleusercontent.com";
    private Constants() {}


}
