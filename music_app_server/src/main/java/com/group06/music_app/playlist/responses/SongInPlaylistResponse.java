package com.group06.music_app.playlist.responses;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SongInPlaylistResponse {
    private Long id;
    private String name;
    private String authorName;
    private String singerName;
    private String audioUrl;
    private String coverImageUrl;
    private long duration;
}

