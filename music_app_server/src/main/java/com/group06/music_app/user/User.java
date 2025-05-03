package com.group06.music_app.user;

import com.group06.music_app.common.BaseEntity;
import com.group06.music_app.song.Playlist;
import com.group06.music_app.song.Song;
import com.group06.music_app.song.SongHistory;
import com.group06.music_app.user_action.Comment;
import com.group06.music_app.user_action.CommentLike;
import com.group06.music_app.user_action.PlaylistLike;
import com.group06.music_app.user_action.SongLike;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginMethod loginMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean accountLocked;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private OTP otp;

    @OneToMany(mappedBy = "user")
    private List<Song> songs;

    @OneToMany(mappedBy = "user")
    private List<Playlist> playlists;

    @OneToMany(mappedBy = "user")
    private List<SongLike> songLikes;

    @OneToMany(mappedBy = "user")
    private List<PlaylistLike> playlistLikes;

    @OneToMany(mappedBy = "user")
    private List<Comment> comments;

    @OneToMany(mappedBy = "user")
    private List<CommentLike> commentLikes;

    @OneToMany(mappedBy = "user")
    private List<SongHistory> songHistories;
}