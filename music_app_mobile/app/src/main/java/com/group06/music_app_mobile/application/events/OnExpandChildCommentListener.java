package com.group06.music_app_mobile.application.events;

import com.group06.music_app_mobile.application.adapters.CommentAdapter;

public interface OnExpandChildCommentListener {
    void displayChild(Long commentId, CommentAdapter adapter);
}
