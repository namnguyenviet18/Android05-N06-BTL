package com.group06.music_app_mobile.api_client.responses;


import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistResponse {


    private Long id;
    private String name;
    private boolean isPublic;
    private String coverImageUrl;
    private Long userId;

    private int songCount;
    private int likeCount;

    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private Long createdBy;
    private Long lastModifiedBy;

}