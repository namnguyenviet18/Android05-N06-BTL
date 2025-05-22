package com.group06.music_app_mobile.api_client.responses;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PlaylistResponse {
    private Long id;
    private String name;
    private boolean isPublic;
    private String coverImageUrl;
    private Long userId;
    private int songCount;
    private int likeCount;
}