package com.group06.music_app_mobile.application.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.UserApi;
import com.group06.music_app_mobile.app_utils.StorageService;
import com.group06.music_app_mobile.api_client.requests.EditProfileRequest;
import com.group06.music_app_mobile.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText editTextFirstName;
    private EditText editTextLastName;
    private Button buttonUpdateProfile;
    private StorageService storageService;
    private UserApi userApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_user);

        // Ánh xạ các thành phần giao diện
        toolbar = findViewById(R.id.toolbar);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        buttonUpdateProfile = findViewById(R.id.buttonUpdateProfile);

        // Khởi tạo StorageService và UserApi
        storageService = StorageService.getInstance(this);
        userApi = ApiClient.getClient(this).create(UserApi.class);

        setupToolbar();
        loadUserProfile();
        setupUpdateButton();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadUserProfile() {
        // Lấy thông tin người dùng từ StorageService
        User currentUser = storageService.getUser();
        if (currentUser != null) {
            editTextFirstName.setText(currentUser.getFirstName());
            editTextLastName.setText(currentUser.getLastName());
        } else {
            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupUpdateButton() {
        buttonUpdateProfile.setOnClickListener(v -> updateProfile());
    }

    private void updateProfile() {
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();

        // Kiểm tra đầu vào
        if (firstName.isEmpty()) {
            editTextFirstName.setError("Tên là bắt buộc");
            editTextFirstName.requestFocus();
            return;
        }
        if (lastName.isEmpty()) {
            editTextLastName.setError("Họ là bắt buộc");
            editTextLastName.requestFocus();
            return;
        }

        // Tạo đối tượng EditProfileRequest
        EditProfileRequest request = new EditProfileRequest(firstName, lastName);

        // Gửi yêu cầu cập nhật lên server
        sendUpdateRequest(request);
    }

    private void sendUpdateRequest(EditProfileRequest request) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Lấy JWT token từ StorageService
        String jwtToken = storageService.getAccessToken();
        if (jwtToken == null || jwtToken.isEmpty()) {
            progressDialog.dismiss();
            Toast.makeText(this, "Please log in to continue", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Gửi request cập nhật profile
        Call<User> call = userApi.updateProfile("Bearer " + jwtToken, request);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    // Cập nhật thông tin người dùng trong StorageService
                    storageService.setUser(response.body());
                    Toast.makeText(UpdateProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                    // Trả kết quả về AccountFragment
                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    String errorMessage = "Failed to update profile";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(UpdateProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(UpdateProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}