package com.group06.music_app.comment;

import com.group06.music_app.comment.responses.CommentResponse;
import com.group06.music_app.user.User;
import com.group06.music_app.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CommentMapper {

    private final UserMapper userMapper;

    public CommentResponse toCommentResponse(Comment comment, User user) {
        if(comment.getCommentLikes() == null) {
            comment.setCommentLikes(new ArrayList<>());
        }
        if(comment.getDescendants() == null) {
            comment.setDescendants(new ArrayList<>());
        }
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .user(userMapper.toUserResponse(comment.getUser()))
                .likes(new ArrayList<>())
                .likeCount(comment.getCommentLikes().size())
                .liked(comment.getCommentLikes().stream()
                        .anyMatch(like ->
                                like.getUser()
                                        .getId()
                                        .equals(user.getId())))

                .descendants(new ArrayList<>())
                .descendantCount(comment.getDescendants().size())
                .parent(comment.getParent() != null ? toCommentResponse(comment.getParent(), user) : null)
                .isDeleted(comment.isDeleted())
                .createdDate(comment.getCreatedDate())
                .lastModifiedDate(comment.getLastModifiedDate())
                .createdBy(comment.getCreatedBy())
                .lastModifiedBy(comment.getLastModifiedBy())
                .build();
    }
}
