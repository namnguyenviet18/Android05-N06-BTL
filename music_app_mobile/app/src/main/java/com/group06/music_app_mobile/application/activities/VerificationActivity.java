package com.group06.music_app_mobile.application.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.AuthApi;
import com.group06.music_app_mobile.api_client.requests.AuthenticationRequest;
import com.group06.music_app_mobile.api_client.requests.VerifyOtpRequest;
import com.group06.music_app_mobile.api_client.responses.AuthenticationResponse;
import com.group06.music_app_mobile.app_utils.StorageService;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
                findViewById(R.id.otpBox5),
                findViewById(R.id.otpBox6)
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
                String action = getIntent().getStringExtra("action");
                Log.e("DEBUG", "HEHEHE ACTION: " + action);
                // Here you would typically verify the OTP with your backend
                verifyOtp(email, otp.toString(), action);
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

    private void verifyOtp(String email, String otp, String action) {
        Log.e("DEBUG", "HEHEHE1 ACTION: " + action);
        Log.e("DEBUG", "HEHEHE2 bool: " + Objects.equals(action, "ChangePassword"));
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail(email);
        request.setOtp(otp);

        AuthApi authApi = ApiClient.getClient(getApplicationContext()).create(AuthApi.class);
        Call<AuthenticationResponse> call = authApi.verifyOtp(request);

        call.enqueue(new Callback<AuthenticationResponse>() {
            @Override
            public void onResponse(Call<AuthenticationResponse> call, Response<AuthenticationResponse> response) {

                int code = response.code();

                if (code == 200 && response.body() != null) {
                    AuthenticationResponse authResponse = response.body();
                    String accessToken = authResponse.getAccessToken();
                    String refreshToken = authResponse.getRefreshToken();

                    // Lưu token
                    StorageService storageService = StorageService.getInstance(VerificationActivity.this);
                    storageService.setAccessToken(accessToken);
                    storageService.setRefreshToken(refreshToken);

                    if (Objects.equals(action, "ChangePassword")){
                        Intent intent = new Intent(VerificationActivity.this, ChangePasswordActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(VerificationActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                } else {
                    String errorMessage = switch (code) {
                        case 400 -> "Mã OTP không hợp lệ";
                        case 401 -> "Mã OTP sai";
                        default -> "Xác thực OTP thất bại: " + code;
                    };
                    Toast.makeText(VerificationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthenticationResponse> call, Throwable t) {
                Toast.makeText(VerificationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}