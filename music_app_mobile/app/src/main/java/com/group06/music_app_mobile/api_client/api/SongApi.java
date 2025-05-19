package com.group06.music_app_mobile.api_client.api;

import com.group06.music_app_mobile.models.Song;

import okhttp3.ResponseBody;
import retrofit2.Call; // Sử dụng retrofit2.Call thay vì okhttp3.Call
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SongApi {

    @GET("song/like/{song-id}")
    Call<Boolean> handleClickLikeSong(@Path("song-id") Long songId);

    @GET("song/file/load")
    Call<ResponseBody> loadFile(@Query("fullUrl") String fullUrl);

    @GET("song/{id}")
    Call<Song> getSongById(@Path("id") Long songId);
}