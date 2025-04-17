package com.group06.music_app_mobile.app_utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;

public class StorageService {

    private static final String PREF_NAME = "auth-prefs";

    private static StorageService instance;

    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;

    private StorageService() {}

    public static StorageService getInstance(Context context) {
        if(instance == null) {
            synchronized (StorageService.class) {
                instance = new StorageService();
                SharedPreferences pref = context.getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );
                preferences = pref;
                editor = pref.edit();
            }
        }
        return instance;
    }

    public void setAccessToken(@NotNull String accessToken) {
        setString(Constants.ACCESS_TOKEN, accessToken);
    }

    public String getAccessToken() {
        return getString(Constants.ACCESS_TOKEN, null);
    }

    public void setRefreshToken(@NotNull String refreshToken) {
        setString(Constants.REFRESH_TOKEN, refreshToken);
    }

    public String getRefreshToken() {
        return getString(Constants.REFRESH_TOKEN, null);
    }

    public void launchApp() {
        setBoolean(Constants.IS_FIRST_LAUNCH, false);
    }
    public boolean isFirstLaunchApp() {
        return getBoolean(Constants.IS_FIRST_LAUNCH, true);
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null && !getAccessToken().isBlank();
    }

    public void setString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public void setBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void setInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public void setFloat(String key, float value) {
        editor.putFloat(key, value);
        editor.apply();
    }

    public void setLong(String key, long value) {
        editor.putLong(key, value);
        editor.apply();
    }

    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public float getFloat(String key, float defaultValue) {
        return preferences.getFloat(key, defaultValue);
    }

    public long getLong(String key, long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }

    public void remove(String key) {
        editor.remove(key);
        editor.apply();
    }

    public void clearAll() {
        editor.clear();
        editor.apply();
    }

}
