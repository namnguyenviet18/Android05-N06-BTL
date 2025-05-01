package com.group06.music_app_mobile.application.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.group06.music_app_mobile.application.fragments.CoverImageFragment;
import com.group06.music_app_mobile.application.fragments.LyricFragment;

public class PlayPagerAdapter extends FragmentStateAdapter {
    public PlayPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ?
                new LyricFragment() :
                new CoverImageFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
