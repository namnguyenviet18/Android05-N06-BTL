package com.group06.music_app_mobile.api_client.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequest {
    private Long id;
    private String content;
    private Long parentId;
    private Long songId;
}
