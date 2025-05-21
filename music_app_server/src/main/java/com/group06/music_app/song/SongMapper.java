package com.group06.music_app.song;

import com.group06.music_app.song.response.SongResponse;
import com.group06.music_app.user.User;
import com.group06.music_app.user.UserMapper;
import jdk.jfr.Registered;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class SongMapper {

    private final UserMapper userMapper;

    public SongResponse toSongResponse(Song song, User user) {
        if(song.getSongLikes() == null) {
            song.setSongLikes(new ArrayList<>());
        }
        if(song.getComments() == null) {
            song.setComments(new ArrayList<>());
        }
        return SongResponse.builder()
                .id(song.getId())
                .name(song.getName())
                .authorName(song.getAuthorName())
                .singerName(song.getSingerName())
                .audioUrl(song.getAudioUrl())
                .coverImageUrl(song.getCoverImageUrl())
                .lyrics(song.getLyrics())
                .isPublic(song.isPublic())
                .fileName(song.getFileName())
                .fileExtension(song.getFileExtension())
                .duration(song.getDuration())
                .likeCount(song.getSongLikes().size())
                .commentCount(song.getComments().size())
                .comments(Collections.emptyList())
                .likes(Collections.emptyList())
                .liked(song.getSongLikes().stream()
                        .anyMatch(like ->
                                like.getUser()
                                        .getId()
                                        .equals(user.getId())))
                .isDeleted(song.isDeleted())
                .createdBy(song.getCreatedBy())
                .createdDate(song.getCreatedDate())
                .lastModifiedDate(song.getLastModifiedDate())
                .lastModifiedBy(song.getLastModifiedBy())
                .build();

    }

}
