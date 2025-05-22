package com.group06.music_app_mobile.api_client.api;

import com.group06.music_app_mobile.models.Playlist;
import com.group06.music_app_mobile.api_client.responses.PlaylistResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface PlaylistApi {

    // Create playlist (multipart form)
    @Multipart
    @POST("playlist/")
    Call<PlaylistResponse> createPlaylist(
            @Part("name") String name,
            @Part("isPublic") boolean isPublic,
            @Part MultipartBody.Part coverImage,
            @Header("Authorization") String authToken
    );

    // Get all playlists
    @GET("playlist/")
    Call<List<Playlist>> getAllPlaylists();

    // Get current user's playlists
    @GET("playlist/user")
    Call<List<Playlist>> getUserPlaylists(@Header("Authorization") String authToken);

    // Get specific playlist by ID
    @GET("playlist/{playlistId}")
    Call<Playlist> getPlaylistById(
            @Path("playlistId") Long playlistId,
            @Header("Authorization") String authToken
    );

    // Update playlist (JSON body)
    @PUT("playlist/{playlistId}")
    Call<Playlist> updatePlaylist(
            @Path("playlistId") Long playlistId,
            @Body Playlist playlist
    );

    // Delete playlist (soft delete)
    @DELETE("playlist/{playlistId}")
    Call<String> deletePlaylist(@Path("playlistId") Long playlistId);

    // Toggle song in playlist
    @POST("playlist/{playlistId}/songs/{songId}")
    Call<ResponseBody> toggleSongInPlaylist(
            @Path("playlistId") Long playlistId,
            @Path("songId") Long songId,
            @Header("Authorization") String authToken
    );

    // Toggle like playlist
    @POST("playlist/{playlistId}/like")
    Call<String> toggleLikePlaylist(
            @Path("playlistId") Long playlistId,
            @Header("Authorization") String authToken
    );
}