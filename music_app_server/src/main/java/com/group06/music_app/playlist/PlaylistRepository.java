package com.group06.music_app.playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    List<Playlist> findByUserIdAndIsDeletedFalse(Long userId);

    Optional<Playlist> findByIdAndIsDeletedFalse(Long id);
}