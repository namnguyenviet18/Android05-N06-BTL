package com.group06.music_app_mobile.application.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.AuthApi;
import com.group06.music_app_mobile.api_client.api.UserApi;
import com.group06.music_app_mobile.api_client.responses.AuthenticationResponse;
import com.group06.music_app_mobile.app_utils.Constants;
import com.group06.music_app_mobile.app_utils.StorageService;
import com.group06.music_app_mobile.application.activities.LoginActivity;
import com.group06.music_app_mobile.application.activities.UpdateProfileActivity;
import com.group06.music_app_mobile.databinding.FragmentAccountBinding;
import com.group06.music_app_mobile.models.User;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AccountFragment extends Fragment {

    private static final String TAG = "AccountFragment";
    private FragmentAccountBinding binding;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> updateProfileLauncher; // Thêm launcher cho UpdateProfileActivity
    private StorageService storageService;
    private AuthApi authApi;
    private UserApi userApi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        authApi = ApiClient.getClient(requireContext()).create(AuthApi.class);
        userApi = ApiClient.getClient(requireContext()).create(UserApi.class);

        // Khởi tạo launcher để chọn ảnh
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                if (imageUri != null) {
                    uploadAvatar(imageUri);
                }
            }
        });

        // Khởi tạo launcher để xử lý kết quả từ UpdateProfileActivity
        updateProfileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                // Làm mới thông tin người dùng sau khi cập nhật
                getProfile();
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            }
        });

        init();
        setupListeners();
        return binding.getRoot();
    }

    private void init() {
        storageService = StorageService.getInstance(requireContext());
        if (storageService.getUser() == null) {
            getProfile();
        }
        displayUserInfo();
    }

    private void setupListeners() {
        binding.logout.setOnClickListener(view -> logout());

        // Xử lý sự kiện click vào icon chỉnh sửa ảnh đại diện
        binding.editAvatar.setOnClickListener(v -> pickImage());

        // Xử lý sự kiện click vào Edit Profile
        binding.editProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), UpdateProfileActivity.class);
            updateProfileLauncher.launch(intent);
        });
    }

    private void displayUserInfo() {
        User user = storageService.getUser();
        binding.userEmail.setText(user != null ? user.getEmail() : "");
        binding.userFirstname.setText(user != null ? user.getFirstName() : "");
        binding.userLastName.setText(user != null ? user.getLastName() : "");
        binding.fullName.setText(user != null ? user.getFullName() : "");
        displayUserAvatar();
    }

    private void displayUserAvatar() {
        User user = storageService.getUser();
        if (user != null) {
            String avatarUrl = user.getAvatarUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(requireContext())
                        .load(avatarUrl.startsWith("https") ? avatarUrl : (Constants.FILE_LOAD_ENDPOINT + avatarUrl))
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_fill)
                        .error(R.drawable.ic_user_fill)
                        .into(binding.avatar);
            } else {
                binding.avatar.setImageResource(R.drawable.ic_user_fill);
            }
        }
    }

    private void getProfile() {
        Call<User> call = userApi.getProfile();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    storageService.setUser(response.body());
                    displayUserInfo();
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Network error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Logging out...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Call<Void> call = userApi.logout();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    storageService.setTokens(new AuthenticationResponse("", ""));
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(requireContext(), "Logout failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "Network error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void uploadAvatar(Uri imageUri) {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Uploading avatar...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            File file = uriToFile(imageUri);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", file.getName(), requestFile);

            String jwtToken = storageService.getAccessToken();
            if (jwtToken == null || jwtToken.isEmpty()) {
                progressDialog.dismiss();
                Toast.makeText(requireContext(), "Please log in to continue", Toast.LENGTH_SHORT).show();
                return;
            }

            Call<User> call = userApi.changeAvatar("Bearer " + jwtToken, body);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful() && response.body() != null) {
                        storageService.setUser(response.body());
                        displayUserAvatar();
                        Toast.makeText(requireContext(), "Avatar updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to update avatar", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                    progressDialog.dismiss();
                    Log.e(TAG, "Upload error: " + t.getMessage(), t);
                    Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            progressDialog.dismiss();
            Log.e(TAG, "Error converting URI to File: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error preparing image", Toast.LENGTH_SHORT).show();
        }
    }

    private File uriToFile(Uri uri) throws Exception {
        File file = new File(requireContext().getCacheDir(), "temp_avatar_" + System.currentTimeMillis() + ".jpg");
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return file;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}