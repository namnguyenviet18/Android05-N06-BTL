package com.group06.music_app.song;

import com.group06.music_app.common.BaseEntity;
import com.group06.music_app.playlist.SongPlaylist;
import com.group06.music_app.song_history.SongHistory;
import com.group06.music_app.user.User;
import com.group06.music_app.comment.Comment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Song extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String authorName;

    @Column(nullable = false)
    private String singerName;

    @Column(nullable = false)
    private String audioUrl;

    @Column(nullable = false)
    private String coverImageUrl;

    @Column(nullable = false)
    private String lyrics;

    @Column(nullable = false)
    private boolean isPublic;

    @Column(nullable = false)
    private boolean isDeleted;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileExtension;

    @Column(nullable = false)
    private Long duration;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "song")
    private List<SongLike> songLikes;

    @OneToMany(mappedBy = "song")
    private List<Comment> comments;

    @OneToMany(mappedBy = "song")
    private List<SongPlaylist> songPlaylists;

    @OneToMany(mappedBy = "song")
    private List<SongHistory> songHistories;
}
