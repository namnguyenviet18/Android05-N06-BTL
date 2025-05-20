package com.group06.music_app.comment.responses;

import com.group06.music_app.comment.Comment;
import com.group06.music_app.comment.CommentLike;
import com.group06.music_app.user.User;
import com.group06.music_app.user.UserResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentResponse {
    private Long id;
    private UserResponse user;
    private String content;
    private List<Comment> descendants;
    private CommentResponse parent;
    private boolean liked;
    private long likeCount;
    private long descendantCount;
    private List<CommentLike> likes;
    private boolean isDeleted;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private Long createdBy;
    private Long lastModifiedBy;
}
