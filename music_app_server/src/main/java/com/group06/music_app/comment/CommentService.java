package com.group06.music_app.comment;

import com.group06.music_app.comment.requests.CommentRequest;
import com.group06.music_app.comment.responses.CommentResponse;
import com.group06.music_app.song.Song;
import com.group06.music_app.song.SongService;
import com.group06.music_app.user.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository repository;
    private final SongService songService;
    private final CommentMapper mapper;

    public CommentResponse createComment(@NotNull CommentRequest request, Authentication currentUser) {
        User user = (User) currentUser.getPrincipal();
        Song song = songService.findSongById(request.getSongId());
        Comment newComment = Comment.builder()
                .content(request.getContent())
                .song(song)
                .user(user)
                .build();
        if(request.getParentId() != null) {
            Optional<Comment> parentOpt = repository.findById(request.getParentId());
            if(parentOpt.isPresent()) {
                Comment parent = parentOpt.get();
                newComment.setParent(parent);
                if(parent.getRoot() != null) {
                    newComment.setRoot(parent.getRoot());
                } else {
                    newComment.setRoot(parent);
                }
            }
        }

        Comment savedComment = repository.save(newComment);
        return mapper.toCommentResponse(savedComment, user);

    }


    public List<CommentResponse> getDescendants(Long commentRootId, Authentication currentUser) {
        User user = (User) currentUser.getPrincipal();
        Optional<Comment> rootOpt = repository.findById(commentRootId);
        if (rootOpt.isEmpty()) {
            return new ArrayList<>();
        }
        Comment root = rootOpt.get();
        return root.getDescendants()
                .stream()
                .map(comment -> mapper.toCommentResponse(comment, user))
                .toList();
    }

    public void clickLikeComment(Long commentId, Authentication currentUser) {
        User user = (User) currentUser.getPrincipal();
        Optional<Comment> commentOpt = repository.findById(commentId);
        if(commentOpt.isEmpty()) {
            throw new EntityNotFoundException("Comment needs to be identified!");
        }
        Comment comment = commentOpt.get();
        Optional<CommentLike> existingLikeOpt = comment.getCommentLikes()
                .stream()
                .filter(like -> like.getUser().getId().equals(user.getId()))
                .findFirst();
        if(existingLikeOpt.isPresent()) {
            CommentLike existingLike = existingLikeOpt.get();
            comment.getCommentLikes().remove(existingLike);
        } else {
            CommentLike commentLike = CommentLike.builder()
                    .user(user)
                    .comment(comment)
                    .build();
            comment.getCommentLikes().add(commentLike);
        }
        repository.save(comment);
    }

    public List<CommentResponse> getCommentsBySong(Long songId, Authentication currentUser) {
        User user = (User) currentUser.getPrincipal();
        List<Comment> topLevelComments = repository.findByRootIsNullAndSongId(songId);

        return topLevelComments
                .stream()
                .map(comment -> mapper.toCommentResponse(comment, user))
                .toList();
    }
}
