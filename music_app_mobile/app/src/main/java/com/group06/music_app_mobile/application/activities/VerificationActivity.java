package com.group06.music_app_mobile.application.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.group06.music_app_mobile.R;

public class VerificationActivity extends AppCompatActivity {

    private EditText[] otpBoxes;
    private Button verifyButton;
    private TextView timerText;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        // Initialize OTP boxes
        otpBoxes = new EditText[]{
                findViewById(R.id.otpBox1),
                findViewById(R.id.otpBox2),
                findViewById(R.id.otpBox3),
                findViewById(R.id.otpBox4),
                findViewById(R.id.otpBox5)
        };

        verifyButton = findViewById(R.id.changeButton);
        timerText = findViewById(R.id.timerText);

        // Get email from intent
        String email = getIntent().getStringExtra("email");

        // Start countdown timer (50 seconds)
        startTimer();

        // Set click listener for Verify button
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder otp = new StringBuilder();
                for (EditText otpBox : otpBoxes) {
                    String digit = otpBox.getText().toString().trim();
                    if (digit.isEmpty()) {
                        Toast.makeText(VerificationActivity.this,
                                "Please enter all OTP digits", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    otp.append(digit);
                }

                // Here you would typically verify the OTP with your backend
                // For demo purposes, we'll just show a success message
                Toast.makeText(VerificationActivity.this,
                        "OTP Verified Successfully", Toast.LENGTH_SHORT).show();

                // You might want to proceed to password reset screen here
            }
        });
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(50000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                timerText.setText(String.format("00:%02d", seconds));
            }

            @Override
            public void onFinish() {
                timerText.setText("00:00");
                Toast.makeText(VerificationActivity.this,
                        "OTP expired. Please request a new one", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}