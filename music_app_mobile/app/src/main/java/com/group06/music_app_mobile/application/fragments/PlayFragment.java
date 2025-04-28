package com.group06.music_app_mobile.application.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.application.activities.PlayActivity;
import com.group06.music_app_mobile.databinding.FragmentPlayBinding;

public class PlayFragment extends Fragment {

    private FragmentPlayBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPlayBinding.inflate(inflater, container, false);
        binding.buttonPlay.setOnClickListener(view -> {
            Intent intent = new Intent(requireActivity(), PlayActivity.class);
            startActivity(intent);
        });
        return binding.getRoot();
    }
}