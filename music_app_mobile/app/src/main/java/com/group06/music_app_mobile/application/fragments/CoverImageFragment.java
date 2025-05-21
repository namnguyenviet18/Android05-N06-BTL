package com.group06.music_app_mobile.application.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.app_utils.Constants;
import com.group06.music_app_mobile.application.activities.PlayActivity;
import com.group06.music_app_mobile.databinding.FragmentCoverImageBinding;
import com.group06.music_app_mobile.models.Song;

public class CoverImageFragment extends Fragment {

    private FragmentCoverImageBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCoverImageBinding.inflate(inflater, container, false);
        displayImage();
        return binding.getRoot();
    }


    private void displayImage() {
        PlayActivity playActivity = getPlayActivity();
        if(playActivity == null) {
            return;
        }
        Song song = playActivity.getSong();
        Glide.with(this)
                .load(Constants.FILE_LOAD_ENDPOINT + song.getCoverImageUrl())
                .centerCrop()
                .into(binding.coverImageView);
    }

    private PlayActivity getPlayActivity() {
        return (PlayActivity) getActivity();
    }
}