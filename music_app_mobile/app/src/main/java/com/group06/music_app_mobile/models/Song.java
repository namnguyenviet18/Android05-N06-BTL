package com.group06.music_app_mobile.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Song extends BaseEntity {
    private String name;
    private String authorName;
    private String singerName;
    private String audioUrl;
    private String coverImageUrl;
    private String lyrics;
    private long likeCount;
    private long commentCount;
    private List<SongLike> likes;
    private List<Comment> comments;
    private boolean isPublic;
    private boolean isDeleted;
    private boolean isLiked;
    private User user;
    private long viewCount;
}