package com.group06.music_app.playlist;

import com.group06.music_app.playlist.requests.PlaylistRequest;
import com.group06.music_app.playlist.responses.PlaylistDetailResponse;
import com.group06.music_app.playlist.responses.PlaylistResponse;
import com.group06.music_app.playlist.responses.SongInPlaylistResponse;
import com.group06.music_app.song.Song;
import com.group06.music_app.song.SongMapper;
import com.group06.music_app.song.SongRepository;
import com.group06.music_app.song.SongService;
import com.group06.music_app.song.response.FileStoreResult;
import com.group06.music_app.song.response.SongResponse;
import com.group06.music_app.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private SongPlaylistRepository songPlaylistRepository;

    @Autowired
    private PlaylistLikeRepository playlistLikeRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private SongService songService;

    @Autowired
    private SongMapper songMapper;

    @Transactional
    public PlaylistResponse createPlaylist(String name, boolean isPublic, MultipartFile coverImage, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        String coverImageUrl = null;
        if (coverImage != null && !coverImage.isEmpty()) {
            FileStoreResult fileStoreResult = songService.storeFile(coverImage);
            if (fileStoreResult != null) {
                coverImageUrl = fileStoreResult.getFilePath();
            }
        }

        Playlist playlist = Playlist.builder()
                .name(name)
                .isPublic(isPublic)
                .coverImageUrl(coverImageUrl)
                .user(user)
                .isDeleted(false)
                .build();

        playlistRepository.save(playlist);
        PlaylistResponse response = mapToPlaylistResponse(playlist);
        return response;
    }

    private PlaylistResponse mapToPlaylistResponse(Playlist playlist) {
        return PlaylistResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .isPublic(playlist.isPublic())
                .coverImageUrl(playlist.getCoverImageUrl())
                .userId(playlist.getUser().getId())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PlaylistDetailResponse> getPlaylists() {
        List<Playlist> listPlaylist = playlistRepository.findAll();
        return listPlaylist.stream()
                .map(playlist -> PlaylistDetailResponse.builder()
                        .id(playlist.getId())
                        .name(playlist.getName())
                        .isPublic(playlist.isPublic())
                        .coverImageUrl(playlist.getCoverImageUrl())
                        .userId(playlist.getUser().getId())
                        .songCount(playlist.getSongPlaylists().size())
                        .likeCount(playlist.getPlaylistLikes().size())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlaylistDetailResponse> getLimitedPlaylists(int limit) {
        if (limit <= 0) {
            limit = 10;
        }
        List<Playlist> listPlaylist = playlistRepository.findAll(PageRequest.of(0, limit)).getContent();
        return listPlaylist.stream()
                .map(playlist -> PlaylistDetailResponse.builder()
                        .id(playlist.getId())
                        .name(playlist.getName())
                        .isPublic(playlist.isPublic())
                        .coverImageUrl(playlist.getCoverImageUrl())
                        .userId(playlist.getUser().getId())
                        .songCount(playlist.getSongPlaylists().size())
                        .likeCount(playlist.getPlaylistLikes().size())
                        .build())
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<PlaylistDetailResponse> getUserPlaylists(Long userId) {
        List<Playlist> playlists = playlistRepository.findByUserIdAndIsDeletedFalse(userId);
        return playlists.stream()
                .map(playlist -> PlaylistDetailResponse.builder()
                        .id(playlist.getId())
                        .name(playlist.getName())
                        .isPublic(playlist.isPublic())
                        .coverImageUrl(playlist.getCoverImageUrl())
                        .userId(playlist.getUser().getId())
                        .songCount(playlist.getSongPlaylists().size())
                        .likeCount(playlist.getPlaylistLikes().size())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PlaylistDetailResponse getPlaylistById(Long playlistId, User user) {
        Playlist playlist = playlistRepository.findByIdAndIsDeletedFalse(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));

        if (!playlist.getUser().getId().equals(user.getId()) && !playlist.isPublic()) {
            throw new SecurityException("Unauthorized access to private playlist");
        }

        return PlaylistDetailResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .isPublic(playlist.isPublic())
                .coverImageUrl(playlist.getCoverImageUrl())
                .userId(playlist.getUser().getId())
                .songCount(playlist.getSongPlaylists().size())
                .likeCount(playlist.getPlaylistLikes().size())
                .build();
    }


    @Transactional
    public Playlist updatePlaylist(Long playlistId, Playlist playlist) {
        Playlist existingPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));
        existingPlaylist.setName(playlist.getName());
        existingPlaylist.setIsPublic(playlist.isPublic());
        existingPlaylist.setCoverImageUrl(playlist.getCoverImageUrl());

        return playlistRepository.save(existingPlaylist);
    }

    @Transactional
    public void deletePlaylist(Long playlistId) {
        Playlist existingPlaylist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));
        existingPlaylist.setIsDeleted(true);
        playlistRepository.save(existingPlaylist);
    }


    @Transactional(readOnly = true)
    public Playlist getPlaylistEntityById(Long playlistId, User user) {
        Playlist playlist = playlistRepository.findByIdAndIsDeletedFalse(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));

        if (!playlist.getUser().getId().equals(user.getId()) && !playlist.isPublic()) {
            throw new SecurityException("Unauthorized access to private playlist");
        }

        return playlist;
    }

    @Transactional
    public boolean toggleSongInPlaylist(Long playlistId, Long songId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        Playlist playlist = getPlaylistEntityById(playlistId, user);

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> {
                    System.out.println("Song with ID " + songId + " not found.");
                    return new EntityNotFoundException("Song not found");
                });

        SongPlaylist existing = songPlaylistRepository.findByPlaylistIdAndSongId(playlistId, songId);

        if (existing != null) {
            songPlaylistRepository.delete(existing);
            // Reorder positions
            List<SongPlaylist> remainingSongs = songPlaylistRepository
                    .findByPlaylistIdOrderByPositionAsc(playlistId);

            for (int i = 0; i < remainingSongs.size(); i++) {
                remainingSongs.get(i).setPosition(i + 1);
                System.out.println("SongPlaylist ID: " + remainingSongs.get(i).getId() + ", New Position: " + (i + 1));
            }

            songPlaylistRepository.saveAll(remainingSongs);
            return false;
        } else {
            Integer maxPosition = songPlaylistRepository.findMaxPositionByPlaylistId(playlistId);
            if (maxPosition == null) {
                maxPosition = 0;
            }
            SongPlaylist songPlaylist = SongPlaylist.builder()
                    .song(song)
                    .playlist(playlist)
                    .position(maxPosition + 1)
                    .build();

            songPlaylistRepository.save(songPlaylist);
            return true;
        }
    }


    @Transactional
    public boolean toggleLikePlaylist(Long playlistId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Playlist playlist = playlistRepository.findByIdAndIsDeletedFalse(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));

        PlaylistLike existing = playlistLikeRepository
                .findByPlaylistIdAndUserId(playlistId, user.getId());

        if (existing != null) {
            playlistLikeRepository.delete(existing);
            return false;
        } else {
            PlaylistLike playlistLike = PlaylistLike.builder()
                    .playlist(playlist)
                    .user(user)
                    .build();
            playlistLikeRepository.save(playlistLike);
            return true;
        }
    }
    @Transactional(readOnly = true)
    public List<SongResponse> getSongsByPlaylistId(Long playlistId, User user) {
        // Verify that the user is authorized to access the playlist
        Playlist playlist = playlistRepository.findByIdAndIsDeletedFalse(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));

        if (!playlist.getUser().getId().equals(user.getId()) && !playlist.isPublic()) {
            throw new SecurityException("Unauthorized access to private playlist");
        }

        // Fetch all SongPlaylist entries for the given playlistId
        List<SongPlaylist> songPlaylists = songPlaylistRepository.findByPlaylistIdOrderByPositionAsc(playlistId);

        // Map SongPlaylist to SongResponse
        List<SongResponse> songs = songPlaylists.stream()
                .map(songPlaylist -> {
                    Song song = songPlaylist.getSong();
                    return songMapper.toSongResponse(song, user);
                })
                .collect(Collectors.toList());

        return songs;
    }
}