package com.group06.music_app_mobile.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Comment extends BaseEntity {
    private User user;
    private String content;
    private List<Comment> descendants;
    private Comment parent;
    private boolean isLiked;
    private long likeCount;
    private long descendantCount;
    private List<CommentLike> likes;
    private boolean isDeleted;
}
