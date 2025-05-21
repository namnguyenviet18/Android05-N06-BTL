package com.group06.music_app_mobile.models;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Song extends BaseEntity implements Serializable {
    private String name;
    private String authorName;
    private String singerName;
    private String audioUrl;
    private String coverImageUrl;
    private String lyrics;
    private String fileName;
    private String fileExtension;
    private long likeCount;
    private long commentCount;
    private List<SongLike> likes;
    private List<Comment> comments;
    private boolean isPublic;
    private boolean isDeleted;
    private boolean liked;
    private User user;
    private long viewCount;
    private long duration;

    public String getName() {
        return name;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getSingerName() {
        return singerName;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public String getLyrics() {
        return lyrics;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public List<SongLike> getLikes() {
        return likes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public boolean isLiked() {
        return liked;
    }

    public User getUser() {
        return user;
    }

    public long getViewCount() {
        return viewCount;
    }
}