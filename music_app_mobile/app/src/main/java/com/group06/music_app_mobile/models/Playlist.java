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
public class Playlist extends BaseEntity  implements Serializable {
    private User user;
    private String name;
    private boolean isPublic;
    private String coverImageUrl;
    private int songCount;
    private int likeCount;
    private List<SongPlaylist> songs;
    private boolean isLiked;
    private List<PlaylistLike> likes;
    private boolean isDeleted;
}
