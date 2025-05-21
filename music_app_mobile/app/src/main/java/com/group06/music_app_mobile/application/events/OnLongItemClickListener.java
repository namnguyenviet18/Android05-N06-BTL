package com.group06.music_app_mobile.application.events;

import com.group06.music_app_mobile.models.Song;

public interface OnLongItemClickListener {
    void onLongItemClick(Song song, android.view.View view);
}
