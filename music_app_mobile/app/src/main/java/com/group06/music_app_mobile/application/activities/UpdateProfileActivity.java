package com.group06.music_app_mobile.application.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.app_utils.ServerDestination;
import com.group06.music_app_mobile.app_utils.StorageService;
import com.group06.music_app_mobile.models.User;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdateProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText editTextFirstName;
    private EditText editTextLastName;
    private Button buttonUpdateProfile;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_user);

        // Ánh xạ các thành phần giao diện bằng findViewById
        toolbar = findViewById(R.id.toolbar);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        buttonUpdateProfile = findViewById(R.id.buttonUpdateProfile);

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
        // Giả định bạn có dữ liệu người dùng hiện tại (có thể lấy từ StorageService hoặc server)
        // Ở đây tôi sẽ tạo dữ liệu giả để demo
        currentUser = new User();
        currentUser.setFirstName("John");
        currentUser.setLastName("Doe");

        // Hiển thị thông tin lên giao diện
        editTextFirstName.setText(currentUser.getFirstName());
        editTextLastName.setText(currentUser.getLastName());
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

        // Cập nhật thông tin người dùng
        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);

        // Gửi yêu cầu cập nhật lên server
        sendUpdateRequest();
    }

    private void sendUpdateRequest() {
        // Lấy JWT token từ StorageService
        String authToken = StorageService.getInstance(this).getAccessToken();
        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cấu hình OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .protocols(Arrays.asList(Protocol.HTTP_1_1, Protocol.HTTP_2)) // Ép dùng HTTP/1.1 hoặc HTTP/2
                .build();

        try {
            // Tạo JSON body chỉ chứa firstName và lastName
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("firstName", currentUser.getFirstName());
            jsonObject.put("lastName", currentUser.getLastName());

            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString());

            // Tạo request với header Authorization chứa JWT token
            Request request = new Request.Builder()
                    .url("http://" + ServerDestination.SERVER_HOST + ":" + ServerDestination.SERVER_PORT + "/api/v1/user/update-profile")
                    .addHeader("Authorization", "Bearer " + authToken) // Thêm JWT token vào header
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(UpdateProfileActivity.this, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(UpdateProfileActivity.this, "Cập nhật profile thành công!", Toast.LENGTH_SHORT).show();
                            // Quay lại màn hình trước
                            onBackPressed();
                        } else {
                            String errorBody = response.body() != null ? response.body().toString() : "Lỗi không xác định";
                            Toast.makeText(UpdateProfileActivity.this, "Lỗi: " + errorBody + " (Code: " + response.code() + ")", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            runOnUiThread(() ->
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }
}