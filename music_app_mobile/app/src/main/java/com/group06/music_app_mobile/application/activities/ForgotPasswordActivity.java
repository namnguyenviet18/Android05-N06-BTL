package com.group06.music_app_mobile.application.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.AuthApi;
import com.group06.music_app_mobile.api_client.requests.RegisterRequest;
import com.group06.music_app_mobile.models.OTP;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
                sendotp(email);
            }
        });
    }

    private void sendotp(String email) {
        AuthApi authApi = ApiClient.getClient(getApplicationContext()).create(AuthApi.class);
        Call<OTP> call = authApi.resendOtp(email);

        call.enqueue(new Callback<OTP>() {
            @Override
            public void onResponse(Call<OTP> call, Response<OTP> response) {
                int code = response.code();
                if (code == 200 || code == 201) {
                    OTP otp = response.body();
                    if (otp != null) {
                        Intent intent = new Intent(ForgotPasswordActivity.this, VerificationActivity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("otp", otp);
                        intent.putExtra("action", "ChangePassword");
                        startActivity(intent);
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this, "Phản hồi từ server không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("DEBUG", "[Signup onResponse]");
                    if (response.errorBody() != null) {
                        try {
                            String errorJson = response.errorBody().string();
                            Log.d("DEBUG", "Server error body: " + errorJson);
                        } catch (IOException e) {
                            Log.e("DEBUG", "Error reading errorBody", e);
                        }
                    }
                    // Xử lý lỗi
                    String errorMessage = switch (code) {
                        case 400 -> "Dữ liệu không hợp lệ";
                        default -> "Có lỗi: " + code;
                    };
                    Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OTP> call, Throwable t) {
                Log.e("DEBUG", "[Signup onFailure]");
                Log.e("DEBUG",  t.getMessage());
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}