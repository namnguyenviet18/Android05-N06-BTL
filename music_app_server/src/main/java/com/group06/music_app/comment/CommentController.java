package com.group06.music_app.comment;

import com.group06.music_app.comment.requests.CommentRequest;
import com.group06.music_app.comment.responses.CommentResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService service;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @RequestBody @NotNull CommentRequest request,
            Authentication currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createComment(request, currentUser));
    }

    @GetMapping("/descendants/{root-id}")
    public ResponseEntity<List<CommentResponse>> getDescendants(
            @PathVariable(name = "root-id") Long commentRootId,
            Authentication currentUser
    ) {
        return ResponseEntity.ok(service.getDescendants(commentRootId, currentUser));
    }

    @GetMapping("/post/{post-id}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(
            @PathVariable(name = "root-id") Long commentRootId,
            Authentication currentUser
    ) {
        return ResponseEntity.ok(service.getDescendants(commentRootId, currentUser));
    }

    @GetMapping("/like/{comment-id}")
    public ResponseEntity<Void> clickLikeComment(
            @PathVariable(name = "comment-id") Long commentId,
            Authentication currentUser
    ) {
        service.clickLikeComment(commentId, currentUser);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
