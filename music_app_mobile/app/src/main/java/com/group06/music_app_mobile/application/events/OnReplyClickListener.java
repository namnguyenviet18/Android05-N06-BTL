package com.group06.music_app_mobile.application.events;

import com.group06.music_app_mobile.app_utils.ReplyCommentHelper;
import com.group06.music_app_mobile.models.Comment;

public interface OnReplyClickListener {
    void replyClicked(ReplyCommentHelper replyCommentHelper);
}
