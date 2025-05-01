package com.group06.music_app_mobile.api_client;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface LoadFileApi {

    @GET
    Call<Map<String, String>> getLyric(@Url String url);
}
