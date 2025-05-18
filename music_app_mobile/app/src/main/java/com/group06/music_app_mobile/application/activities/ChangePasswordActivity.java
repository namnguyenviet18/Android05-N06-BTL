package com.group06.music_app_mobile.application.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.AuthApi;
import com.group06.music_app_mobile.api_client.api.UserApi;
import com.group06.music_app_mobile.api_client.requests.ChangePasswordRequest;
import com.group06.music_app_mobile.api_client.requests.VerifyOtpRequest;
import com.group06.music_app_mobile.api_client.responses.AuthenticationResponse;
import com.group06.music_app_mobile.app_utils.StorageService;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText newPasswordEdittext, confirmPasswordEdittext;
    private Button changeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepassword);

        // Initialize views
        newPasswordEdittext = findViewById(R.id.newPasswordEdittext);
        confirmPasswordEdittext = findViewById(R.id.confirmPasswordEdittext);
        changeButton = findViewById(R.id.changeButton);

        // Set click listener for Send OTP button
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = newPasswordEdittext.getText().toString().trim();
                String confirmPassword = confirmPasswordEdittext.getText().toString().trim();

                // Basic email validation
                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Start VerificationActivity
                changePassword(newPassword, confirmPassword);
            }
        });
    }

    private void changePassword(String newPassword, String confirmPassword) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword(newPassword);
        request.setConfirmationPassword(confirmPassword);

        UserApi userApi = ApiClient.getClient(getApplicationContext()).create(UserApi.class);
        Call<Void> call = userApi.changePassword(request);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                int code = response.code();

                if (code == 202) {
                    Toast.makeText(ChangePasswordActivity.this,
                            "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();

                    // Đóng tất cả Activity và chuyển đến MainActivity
                    Intent intent = new Intent(ChangePasswordActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    String errorMessage = switch (code) {
                        case 400 -> "Dữ liệu không hợp lệ";
                        case 401 -> "Không được phép đổi mật khẩu";
                        case 403 -> "Không có quyền truy cập";
                        default -> "Đổi mật khẩu thất bại: " + code;
                    };
                    Toast.makeText(ChangePasswordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}