package com.group06.music_app_mobile.application.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.group06.music_app_mobile.application.fragments.AccountFragment;
import com.group06.music_app_mobile.application.fragments.DownloadFragment;
import com.group06.music_app_mobile.application.fragments.FavoriteFragment;
import com.group06.music_app_mobile.application.fragments.HomeFragment;
import com.group06.music_app_mobile.application.fragments.PlayFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1: return new FavoriteFragment();
            case 2: return new PlayFragment();
            case 3: return new DownloadFragment();
            case 4: return new AccountFragment();
            default: return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
