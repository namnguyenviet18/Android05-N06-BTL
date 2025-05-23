package com.group06.music_app.playlist.responses;

import com.group06.music_app.song.response.SongResponse;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
@Builder
public class PlaylistDetailResponse {
    private Long id;
    private String name;
    private Boolean isPublic;
    private String coverImageUrl;
    private Long userId;
    private int songCount;
    private int likeCount;
}
