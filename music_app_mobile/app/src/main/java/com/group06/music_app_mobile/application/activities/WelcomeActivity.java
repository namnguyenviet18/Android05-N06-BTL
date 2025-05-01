package com.group06.music_app_mobile.application.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.app_utils.Constants;
import com.group06.music_app_mobile.app_utils.StorageService;
import com.group06.music_app_mobile.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends AppCompatActivity {

    private ActivityWelcomeBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        boolean isFirstWelcome = getIntent().getBooleanExtra(Constants.IS_FIRST_WELCOME, true);

        if (isFirstWelcome) {
            binding.skipButton.setVisibility(View.VISIBLE);
            binding.skipButton.setEnabled(true);
            binding.nextButton.setText("NEXT");
            binding.logo.setImageResource(R.drawable.welcome_first_logo);
            binding.titleText.setText("Welcome to the world\nof music");
            binding.descriptionText.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore.");
        } else {
            binding.nextButton.setText("START");
            binding.logo.setImageResource(R.drawable.welcome_second_logo);
            binding.titleText.setText("Download and save your\nFavorite Music");
            binding.descriptionText.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore.");
            binding.skipButton.setVisibility(View.INVISIBLE);
            binding.skipButton.setEnabled(false);
        }

        binding.nextButton.setOnClickListener(view -> {
            if (isFirstWelcome) {
                Intent intent = new Intent(WelcomeActivity.this, WelcomeActivity.class);
                intent.putExtra(Constants.IS_FIRST_WELCOME, false);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.slide_in_left);
            } else {
                /// to do lần đầu vào app thì user sẽ phải đi qua các màn welcome.
                /// Chỉ khi user đã xem qua các màn welcome thì set trạng thái IS_FIRST_LAUNCH =
                /// FALSE
                /// thì lần sau vào app sẽ vào login | home mà không cần vào welcome nữa
                /// StorageService.getInstance(this).launchApp();
                StorageService.getInstance(this).launchApp();
                Intent intent = new Intent(WelcomeActivity.this, SignupActivity.class);
                startActivity(intent);
            }
            finish();
        });

        binding.skipButton.setOnClickListener(view -> {
            StorageService.getInstance(this).launchApp();
            Intent intent = new Intent(WelcomeActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        });

    }
}