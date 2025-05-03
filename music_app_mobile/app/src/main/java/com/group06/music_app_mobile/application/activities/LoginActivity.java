package com.group06.music_app_mobile.application.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set click listener for the Sign In button
        binding.loginButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        // Set click listener for the "Sign up" text
        binding.signUpLinkTextView.setOnClickListener(view -> {
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
            finish(); // Close the login activity
        });

        // Set click listener for the "Forgot Password" text
        binding.forgotPasswordTextView.setOnClickListener(view -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onBackPressed() {
        // Navigate to SignupActivity instead of going back to WelcomeActivity
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
        finish();
    }
}