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

    public static final String FILE_LOAD_ENDPOINT = SERVER_DOMAIN + "song/file/load?fullUrl=";
    public static final String GOOGLE_CLIENT_ID = "1078270987220-iq6dluklb03qlrsc4rgmfp6hireno8cn.apps.googleusercontent.com";
//    public static final String GOOGLE_CLIENT_ID = "235758457518-ks3d2skub7lka467vvdmiaot65a4eebj.apps.googleusercontent.com";
    private Constants() {}


}
