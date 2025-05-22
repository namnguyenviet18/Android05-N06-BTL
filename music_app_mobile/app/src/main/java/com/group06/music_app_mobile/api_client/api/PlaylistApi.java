package com.group06.music_app_mobile.api_client.api;

import com.group06.music_app_mobile.models.Playlist;
import com.group06.music_app_mobile.models.Song;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PlaylistApi {

    @GET("playlist/list")
    Call<List<Playlist>> getAllPlaylists();

    @GET("playlist/{id}")
    Call<Playlist> getPlaylistById(@Path("id") long playlistId);

    @POST("playlist/add")
    Call<Playlist> addPlaylist(
            @Query("name") String name,
            @Query("isPublic") boolean isPublic,
            @Header("Authorization") String authToken
    );

    @DELETE("playlist/{id}")
    Call<String> deletePlaylist(@Path("id") long playlistId);

    @GET("playlist/{id}/songs")
    Call<List<Song>> getSongsInPlaylist(@Path("id") long playlistId);

    @POST("playlist/{id}/add-song")
    Call<String> addSongToPlaylist(
            @Path("id") long playlistId,
            @Query("songId") long songId,
            @Header("Authorization") String authToken
    );

    @DELETE("playlist/{id}/remove-song")
    Call<String> removeSongFromPlaylist(
            @Path("id") long playlistId,
            @Query("songId") long songId,
            @Header("Authorization") String authToken
    );
}