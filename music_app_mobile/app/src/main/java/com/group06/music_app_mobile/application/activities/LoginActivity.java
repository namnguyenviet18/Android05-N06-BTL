package com.group06.music_app_mobile.application.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.AuthApi;
import com.group06.music_app_mobile.api_client.requests.AuthenticationRequest;
import com.group06.music_app_mobile.api_client.responses.AuthenticationResponse;
import com.group06.music_app_mobile.app_utils.StorageService;
import com.group06.music_app_mobile.databinding.ActivityLoginBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set click listener for the Sign In button
        binding.loginButton.setOnClickListener(view -> {
            String email = binding.emailEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra định dạng email hợp lệ
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            login(email, password);
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

    private void login(String email, String password) {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail(email);
        request.setPassword(password);

        AuthApi authApi = ApiClient.getClient(this).create(AuthApi.class);
        Call<AuthenticationResponse> call = authApi.authenticate(request);

        call.enqueue(new Callback<AuthenticationResponse>() {
            @Override
            public void onResponse(Call<AuthenticationResponse> call, Response<AuthenticationResponse> response) {
                int code = response.code();

                if (code == 200 && response.body() != null) {
                    AuthenticationResponse authResponse = response.body();
                    String accessToken = authResponse.getAccessToken();
                    String refreshToken = authResponse.getRefreshToken();

                    // Lưu token
                    StorageService storageService = StorageService.getInstance(LoginActivity.this);
                    storageService.setAccessToken(accessToken);
                    storageService.setRefreshToken(refreshToken);

                    // Đóng tất cả Activity và chuyển đến Trang chủ (HomeActivity)
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else if (code == 406) {
                    // Chuyển sang màn hình xác thực OTP
                    Intent intent = new Intent(LoginActivity.this, VerificationActivity.class);
//                    intent.putExtra("username", username); // nếu cần dùng ở màn OTP
                    startActivity(intent);
                }
                else {
                    // Các lỗi khác → Toast
                    Toast.makeText(LoginActivity.this,
                            "Đăng nhập thất bại: " + code,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthenticationResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        }
}