package com.group06.music_app.song;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SongService {

    private final SongRepository repository;

    public Song findSongById(Long id) {
        Optional<Song> songOpt = repository.findById(id);
        if(songOpt.isEmpty()) {
            throw new EntityNotFoundException("Song not found!");
        }
        return songOpt.get();
    }
}
