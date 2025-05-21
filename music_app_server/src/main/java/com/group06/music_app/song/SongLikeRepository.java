package com.group06.music_app.song;

import com.group06.music_app.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongLikeRepository extends JpaRepository<SongLike, Long> {
    List<SongLike> findByUser(User user);
}
