package com.group06.music_app.playlist;

import com.group06.music_app.playlist.responses.PlaylistDetailResponse;
import com.group06.music_app.playlist.responses.PlaylistResponse;
import com.group06.music_app.song.response.SongResponse;
import com.group06.music_app.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/playlist")
@Tag(name = "Playlist")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService;


    // Create a new playlist
    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PlaylistResponse> createPlaylist(
            @RequestParam("name") String name,
            @RequestParam("isPublic") boolean isPublic,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage,
            Authentication authentication) {
        PlaylistResponse createdPlaylist = playlistService.createPlaylist(name, isPublic, coverImage, authentication);
        return new ResponseEntity<>(createdPlaylist, HttpStatus.CREATED);
    }

    // Get all playlists
    @GetMapping("/")
    public ResponseEntity<List<PlaylistDetailResponse>> getPlaylists() {
        List<PlaylistDetailResponse> playlists = playlistService.getPlaylists();
        return ResponseEntity.ok(playlists);
    }

    @GetMapping("/limited")
    public ResponseEntity<List<PlaylistDetailResponse>> getLimitedPlaylists(@RequestParam(defaultValue = "5") int limit) {
        List<PlaylistDetailResponse> playlists = playlistService.getLimitedPlaylists(limit);
        return ResponseEntity.ok(playlists);
    }

    // Get all playlists for the current user
    @GetMapping("/user")
    public ResponseEntity<List<PlaylistDetailResponse>> getUserPlaylists(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<PlaylistDetailResponse> playlists = playlistService.getUserPlaylists(user.getId());
        return ResponseEntity.ok(playlists);
    }

    // Get a specific playlist
    @GetMapping("/{playlistId}")
    public ResponseEntity<PlaylistDetailResponse> getPlaylist(@PathVariable Long playlistId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        PlaylistDetailResponse playlist = playlistService.getPlaylistById(playlistId, user);
        return ResponseEntity.ok(playlist);
    }


    // Update a playlist
    @PutMapping("/{playlistId}")
    public ResponseEntity<Playlist> updatePlaylist(@PathVariable Long playlistId,
                                                   @RequestBody Playlist playlist) {
        Playlist updatedPlaylist = playlistService.updatePlaylist(playlistId, playlist);
        return ResponseEntity.ok(updatedPlaylist);
    }

    // Delete a playlist (soft delete)
    @DeleteMapping("/{playlistId}")
    public ResponseEntity<String> deletePlaylist(@PathVariable Long playlistId) {
        playlistService.deletePlaylist(playlistId);
        return ResponseEntity.ok("Playlist deleted successfully");
    }

    // Toggle song in playlist
    @PostMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<String> toggleSongInPlaylist(@PathVariable Long playlistId,
                                                       @PathVariable Long songId, Authentication authentication) {
        boolean isAdded = playlistService.toggleSongInPlaylist(playlistId, songId, authentication);
        String message = isAdded ? "Song added to playlist" : "Song removed from playlist";
        return ResponseEntity.ok(message);
    }

    // Toggle like playlist
    @PostMapping("/{playlistId}/like")
    public ResponseEntity<String> toggleLikePlaylist(@PathVariable Long playlistId, Authentication authentication) {
        boolean isLiked = playlistService.toggleLikePlaylist(playlistId, authentication);
        String message = isLiked ? "Playlist liked" : "Playlist unliked";
        return ResponseEntity.ok(message);
    }
    @GetMapping("/{playlistId}/songs/user")
    public ResponseEntity<List<SongResponse>> getSongsByPlaylistId(
            @PathVariable Long playlistId,
            Authentication authentication
    ) {
        User user = (User) authentication.getPrincipal();
        List<SongResponse> songs = playlistService.getSongsByPlaylistId(playlistId, user);
        return ResponseEntity.ok(songs);
    }
}