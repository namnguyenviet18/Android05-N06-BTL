package com.group06.music_app_mobile.application.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.app_utils.Constants;
import com.group06.music_app_mobile.app_utils.StorageService;
import com.group06.music_app_mobile.databinding.ActivityWelcomeBinding;


public class WelcomeActivity extends AppCompatActivity {

    private ActivityWelcomeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        boolean isFirstWelcome = getIntent().getBooleanExtra(Constants.IS_FIRST_WELCOME, true);

        if(isFirstWelcome) {
            binding.welcomeButton.setText("NEXT");
        } else {
            binding.welcomeButton.setText("START");
        }

        binding.welcomeButton.setOnClickListener(view -> {
            if(isFirstWelcome) {
                Intent intent = new Intent(WelcomeActivity.this, WelcomeActivity.class);
                intent.putExtra(Constants.IS_FIRST_WELCOME, false);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.slide_in_left);
            } else {
                /// to do lần đầu vào app thì user sẽ phải đi qua các màn welcome.
                /// Chỉ khi user đã xem qua các màn welcome thì set trạng thái IS_FIRST_LAUNCH = FALSE
                /// thì lần sau vào app sẽ vào login | home mà không cần vào welcome nữa
                ///StorageService.getInstance(this).launchApp();
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            }

        });

    }
}