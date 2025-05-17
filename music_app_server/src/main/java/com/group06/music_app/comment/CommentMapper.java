package com.group06.music_app.comment;

import com.group06.music_app.comment.responses.CommentResponse;
import com.group06.music_app.user.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CommentMapper {

    public CommentResponse toCommentResponse(Comment comment, User user) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(comment.getUser())
                .likes(new ArrayList<>())
                .isLiked(comment.getCommentLikes().stream()
                        .anyMatch(like ->
                                like.getUser()
                                        .getId()
                                        .equals(user.getId())))

                .likeCount(comment.getCommentLikes().size())
                .descendants(new ArrayList<>())
                .descendantCount(comment.getDescendants().size())
                .parent(comment.getParent() != null ? toCommentResponse(comment.getParent(), user) : null)
                .isDeleted(comment.isDeleted())
                .build();
    }
}
