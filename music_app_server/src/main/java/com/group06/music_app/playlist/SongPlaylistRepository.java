package com.group06.music_app.playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongPlaylistRepository extends JpaRepository<SongPlaylist, Long> {

    SongPlaylist findByPlaylistIdAndSongId(Long playlistId, Long songId);

    List<SongPlaylist> findByPlaylistIdOrderByPositionAsc(Long playlistId);

    @Query("SELECT COALESCE(MAX(sp.position), 0) FROM SongPlaylist sp WHERE sp.playlist.id = :playlistId")
    Integer findMaxPositionByPlaylistId(Long playlistId);
}