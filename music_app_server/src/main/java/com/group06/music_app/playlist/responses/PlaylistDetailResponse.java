package com.group06.music_app.playlist.responses;

import com.group06.music_app.song.response.SongResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlaylistDetailResponse {
    private Long id;
    private String name;
    private Boolean isPublic;
    private String coverImageUrl;
    private Long userId;
    private List<SongInPlaylistResponse> songs;
    private int likeCount;
}
