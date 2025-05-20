package com.group06.music_app_mobile.app_utils;

import com.group06.music_app_mobile.application.adapters.CommentAdapter;
import com.group06.music_app_mobile.models.Comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReplyCommentHelper {
    private Comment comment;
    private CommentAdapter adapter;
    private boolean isChildComment;
    private int position;
}
