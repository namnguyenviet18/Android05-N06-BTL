package com.group06.music_app_mobile.application.events;

import com.group06.music_app_mobile.models.Comment;

public interface OnReplyClickListener {
    void replyClicked(Comment comment, boolean isChildComment);
}
