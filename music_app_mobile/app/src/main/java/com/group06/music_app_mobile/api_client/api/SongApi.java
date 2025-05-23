package com.group06.music_app_mobile.api_client.api;
import com.group06.music_app_mobile.models.Song;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SongApi {

    @Multipart
    @POST("song/add")
    Call<Song> addSong(
            @Part("songName") String songName,
            @Part("authorName") String authorName,
            @Part("singerName") String singerName,
            @Part MultipartBody.Part audioFile,
            @Part MultipartBody.Part coverImage,
            @Part MultipartBody.Part lyricFile,
            @Part("isPublic") boolean isPublic,
            @Header("Authorization") String authToken
    );

    @GET("song/list")
    Call<List<Song>> getAllSongs();

    @DELETE("song/{id}")
    Call<String> deleteSong(@Path("id") long songId);

    @GET("song/liked")
    Call<List<Song>> getLikedSong();

    @GET("song/{id}")
    Call<Song> getSongById(@Path("id") long songId);

    @Multipart
    @POST("song/file/upload")
    Call<String> uploadFile(@Part MultipartBody.Part file);

    @GET("song/file/load")
    Call<ResponseBody> loadFile(@Query("fullUrl") String fullUrl);

    @DELETE("song/file/delete")
    Call<String> deleteFile(@Query("fileUrl") String fileUrl);

    @GET("song/like/{song-id}")
    Call<Boolean> handleClickLikeSong(@Path("song-id") Long songId);
}