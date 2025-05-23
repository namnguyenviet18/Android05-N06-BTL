package com.group06.music_app_mobile.application.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.group06.music_app_mobile.application.fragments.CoverImageFragment;
import com.group06.music_app_mobile.application.fragments.LyricFragment;

public class PlayPagerAdapter extends FragmentStateAdapter {

    private boolean isPlayDownloadedSong;
    public PlayPagerAdapter(@NonNull FragmentActivity fragmentActivity, boolean isPlayDownloadedSong) {
        super(fragmentActivity);

        this.isPlayDownloadedSong = isPlayDownloadedSong;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ?
                new LyricFragment(isPlayDownloadedSong) :
                new CoverImageFragment(isPlayDownloadedSong);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
