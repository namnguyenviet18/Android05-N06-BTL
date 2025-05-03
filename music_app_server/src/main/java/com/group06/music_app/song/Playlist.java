package com.group06.music_app.song;

import com.group06.music_app.common.BaseEntity;
import com.group06.music_app.user.User;
import com.group06.music_app.user_action.PlaylistLike;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "playlists")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Playlist extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean isPublic;

    private String coverImageUrl;

    @Column(nullable = false)
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "playlist")
    private List<PlaylistLike> playlistLikes;

    @OneToMany(mappedBy = "playlist")
    private List<SongPlaylist> songPlaylists;
}
