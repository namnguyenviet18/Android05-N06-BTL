package com.group06.music_app_mobile.application.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.gson.Gson;
import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.AuthApi;
import com.group06.music_app_mobile.api_client.requests.AuthenticationRequest;
import com.group06.music_app_mobile.api_client.requests.GoogleAuthRequest;
import com.group06.music_app_mobile.api_client.responses.AuthenticationResponse;
import com.group06.music_app_mobile.api_client.responses.ExceptionResponse;
import com.group06.music_app_mobile.app_utils.GoogleSignInHelper;
import com.group06.music_app_mobile.app_utils.StorageService;
import com.group06.music_app_mobile.databinding.ActivityLoginBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    private GoogleSignInHelper googleSignInHelper;
    private final int RC_SIGN_IN = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        googleSignInHelper = new GoogleSignInHelper(this, new GoogleSignInHelper.Callback() {
            @Override
            public void onSuccess(GoogleSignInAccount account) {
                String idToken = account.getIdToken();
                if (idToken != null) {
                    authenticateWithGoogle(idToken);
                } else {
                    Toast.makeText(LoginActivity.this, "Không lấy được ID Token từ Google", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.d("[Google Sign in Error]: ", e.getMessage());
                Toast.makeText(LoginActivity.this, "Google Sign-In thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

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

        // Set click listener for the "Sign in with google"
        binding.buttonLoginGoogle.setOnClickListener(view -> {
            googleSignInHelper.startSignIn();
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

    private void authenticateWithGoogle(String idToken) {
        AuthApi authApi = ApiClient.getClient(this).create(AuthApi.class);
        Call<AuthenticationResponse> call = authApi.authenticateWithGoogle(new GoogleAuthRequest(idToken));

        call.enqueue(new Callback<AuthenticationResponse>() {
            @Override
            public void onResponse(Call<AuthenticationResponse> call, Response<AuthenticationResponse> response) {
                if (response.code() == 200 && response.body() != null) {
                    AuthenticationResponse authResponse = response.body();

                    StorageService storageService = StorageService.getInstance(LoginActivity.this);
                    storageService.setAccessToken(authResponse.getAccessToken());
                    storageService.setRefreshToken(authResponse.getRefreshToken());

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else if (response.code() == 400 && response.errorBody() != null) {
                    try {
                        String errorJson = response.errorBody().string();
                        Gson gson = new Gson();
                        ExceptionResponse exceptionResponse = gson.fromJson(errorJson, ExceptionResponse.class);
                        Toast.makeText(LoginActivity.this, exceptionResponse.getMessage(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "Lỗi không xác định", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthenticationResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        googleSignInHelper.handleResult(requestCode, data);
    }
}