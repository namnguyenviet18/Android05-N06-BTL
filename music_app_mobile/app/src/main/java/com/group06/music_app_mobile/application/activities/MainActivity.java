package com.group06.music_app_mobile.application.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;


import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.application.adapters.ViewPagerAdapter;
import com.group06.music_app_mobile.databinding.ActivityMainBinding;
import com.group06.music_app_mobile.models.Comment;
import com.group06.music_app_mobile.models.Song;
import com.group06.music_app_mobile.models.SongLike;
import com.group06.music_app_mobile.models.User;

public class MainActivity extends AppCompatActivity {

    private static final int HOME = 0;
    private static final int FAVORITE = 1;
    private static final int PLAY = 2;
    private static final int DOWNLOAD = 3;
    private static final int ACCOUNT = 4;

    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpViewPager();
        setUpBottomNav();
    }

    private void setUpViewPager() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(viewPagerAdapter);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.bottomNavigationView
                        .getMenu()
                        .getItem(position)
                        .setChecked(true);
            }
        });
    }


    @SuppressLint("NonConstantResourceId")
    private void setUpBottomNav() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                binding.viewPager.setCurrentItem(HOME);
                return true;
            } else if (item.getItemId() == R.id.nav_favorite) {
                binding.viewPager.setCurrentItem(FAVORITE);
                return true;
            } else if (item.getItemId() == R.id.nav_play) {
                binding.viewPager.setCurrentItem(PLAY);
                return true;
            } else if (item.getItemId() == R.id.nav_download) {
                binding.viewPager.setCurrentItem(DOWNLOAD);
                return true;
            } else if (item.getItemId() == R.id.nav_account) {
                binding.viewPager.setCurrentItem(ACCOUNT);
                return true;
            }
            return false;
        });
    }

}