package com.group06.music_app_mobile.application.events;

import com.group06.music_app_mobile.application.adapters.CommentAdapter;
import com.group06.music_app_mobile.models.Comment;

public interface OnExpandChildCommentListener {
    void displayChild(Comment comment, int position);
}
