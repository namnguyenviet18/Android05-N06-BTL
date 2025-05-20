package com.group06.music_app_mobile.application.events;

import com.group06.music_app_mobile.application.adapters.CommentAdapter;

public interface OnLikeClickListener {
    void handleLike(Long id, CommentAdapter commentAdapter);
}
