package com.group06.music_app_mobile.application.activities;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.databinding.ActivityPlayBinding;

public class PlayActivity extends AppCompatActivity {

    private ActivityPlayBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }
}