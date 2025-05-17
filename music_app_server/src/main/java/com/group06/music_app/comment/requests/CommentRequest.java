package com.group06.music_app.comment.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "Content is required!")
    @NotEmpty(message = "Content is required!")
    private String content;
    private Long parentId;
    @NotNull(message = "Song needs to be identified!")
    private Long songId;
}
