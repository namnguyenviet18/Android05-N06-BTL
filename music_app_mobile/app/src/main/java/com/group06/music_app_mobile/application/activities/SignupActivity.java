package com.group06.music_app_mobile.application.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.AuthApi;
import com.group06.music_app_mobile.api_client.requests.AuthenticationRequest;
import com.group06.music_app_mobile.api_client.requests.RegisterRequest;
import com.group06.music_app_mobile.app_utils.GoogleAuthService;
import com.group06.music_app_mobile.databinding.ActivitySignupBinding;
import com.group06.music_app_mobile.models.OTP;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private GoogleAuthService googleAuthService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        googleAuthService = GoogleAuthService.getInstance(this, true);
        signInWithGoogle();

        // Set click listener for the "Sign in" text
        binding.signInLinkTextView.setOnClickListener(view -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close the signup activity
        });


        // Set click listener for the Sign Up button
        binding.signupButton.setOnClickListener(view -> {
            String email = binding.emailEdittext.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();
            String lastname = binding.lastNameEdittext.getText().toString().trim();
            String firstname = binding.firstNameEdittext.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || lastname.isEmpty() || firstname.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra định dạng email hợp lệ
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }


            if (password.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gọi hàm signup
            signup(firstname, lastname, email, password);
        });
    }

    private void signInWithGoogle() {
        binding.buttonLoginGoogle.setOnClickListener(view -> googleAuthService.signIn());
    }

    private void signup(String firstname, String lastname, String email, String password) {
        RegisterRequest request = new RegisterRequest(firstname, lastname, email, password);

        AuthApi authApi = ApiClient.getClient(getApplicationContext()).create(AuthApi.class);
        Call<OTP> call = authApi.register(request);

        call.enqueue(new Callback<OTP>() {
            @Override
            public void onResponse(Call<OTP> call, Response<OTP> response) {
                int code = response.code();
                if (code == 200 || code == 201) {
                    OTP otp = response.body();
                    Log.d("DEBUG", "HEHEHEHE Otp " + otp.getIssuedAt() + " " + otp.getExpireAt());

                    if (otp != null) {


                        // Đăng ký thành công, chuyển sang màn xác thực OTP
                        Intent intent = new Intent(SignupActivity.this, VerificationActivity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("otp", otp); // Gửi token OTP nếu cần
                        intent.putExtra("action", "Signup");
                        startActivity(intent);
                    } else {
                        Toast.makeText(SignupActivity.this, "Phản hồi từ server không hợp lệ", Toast.LENGTH_SHORT).show();
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
                        case 409 -> "Email đã được sử dụng";
                        default -> "Đăng ký thất bại: " + code;
                    };
                    Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OTP> call, Throwable t) {
                Log.e("DEBUG", "[Signup onFailure]");
                Log.e("DEBUG",  t.getMessage());
                Toast.makeText(SignupActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}