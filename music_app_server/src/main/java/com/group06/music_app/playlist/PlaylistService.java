package com.group06.music_app.playlist;

import com.group06.music_app.playlist.requests.PlaylistRequest;
import com.group06.music_app.playlist.responses.PlaylistDetailResponse;
import com.group06.music_app.playlist.responses.PlaylistResponse;
import com.group06.music_app.playlist.responses.SongInPlaylistResponse;
import com.group06.music_app.song.Song;
import com.group06.music_app.song.SongRepository;
import com.group06.music_app.song.SongService;
import com.group06.music_app.song.response.FileStoreResult;
import com.group06.music_app.song.response.SongResponse;
import com.group06.music_app.user.User;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<Playlist> getPlaylists() {
        List<Playlist> listPlaylist = playlistRepository.findAll();
        for (Playlist p : listPlaylist) {
            p.setUser(null);
            p.setPlaylistLikes(Collections.emptyList());
            p.setSongPlaylists(Collections.emptyList());
        }
        return listPlaylist;
    }

    @Transactional(readOnly = true)
    public List<Playlist> getUserPlaylists(Long userId) {
        List<Playlist> playlists = playlistRepository.findByUserIdAndIsDeletedFalse(userId);
        for (Playlist p : playlists) {
            p.setPlaylistLikes(Collections.emptyList());
            p.setSongPlaylists(Collections.emptyList());
        }
        return playlists;
    }

    @Transactional(readOnly = true)
    public PlaylistDetailResponse getPlaylistById(Long playlistId, User user) {
        Playlist playlist = playlistRepository.findByIdAndIsDeletedFalse(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));

        if (!playlist.getUser().getId().equals(user.getId()) && !playlist.isPublic()) {
            throw new SecurityException("Unauthorized access to private playlist");
        }

        // Chuyển list song trong playlist thành SongInPlaylistResponse
        List<SongInPlaylistResponse> songResponses = playlist.getSongPlaylists().stream()
                .map(sp -> {
                    var song = sp.getSong();
                    return SongInPlaylistResponse.builder()
                            .id(song.getId())
                            .name(song.getName())
                            .authorName(song.getAuthorName())
                            .singerName(song.getSingerName())
                            .audioUrl(song.getAudioUrl())
                            .coverImageUrl(song.getCoverImageUrl())
                            .duration(song.getDuration())
                            .build();
                })
                .collect(Collectors.toList());

        return PlaylistDetailResponse.builder()
                .id(playlist.getId())
                .name(playlist.getName())
                .isPublic(playlist.isPublic())
                .coverImageUrl(playlist.getCoverImageUrl())
                .userId(playlist.getUser().getId())
                .songs(songResponses) // <- dùng songResponses nhẹ
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
}