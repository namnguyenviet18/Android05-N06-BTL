package com.group06.music_app_mobile.application.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.group06.music_app_mobile.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button sendOtpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        sendOtpButton = findViewById(R.id.sendOtpButton);

        // Set click listener for Send OTP button
        sendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();

                // Basic email validation
                if (email.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Start VerificationActivity
                Intent intent = new Intent(ForgotPasswordActivity.this,
                        VerificationActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });
    }
}