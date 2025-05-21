package com.group06.music_app.playlist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaylistLikeRepository extends JpaRepository<PlaylistLike, Long> {

    PlaylistLike findByPlaylistIdAndUserId(Long playlistId, Long userId);
}