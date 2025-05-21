package com.group06.music_app_mobile.application.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.AuthApi;
import com.group06.music_app_mobile.api_client.api.UserApi;
import com.group06.music_app_mobile.api_client.responses.AuthenticationResponse;
import com.group06.music_app_mobile.app_utils.Constants;
import com.group06.music_app_mobile.app_utils.StorageService;
import com.group06.music_app_mobile.application.activities.LoginActivity;
import com.group06.music_app_mobile.databinding.FragmentAccountBinding;
import com.group06.music_app_mobile.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;

    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private StorageService storageService;
    private AuthApi authApi;
    private UserApi userApi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        authApi = ApiClient.getClient(requireContext()).create(AuthApi.class);
        userApi = ApiClient.getClient(requireContext()).create(UserApi.class);
        init();
        binding.logout.setOnClickListener(view -> {
            logout();
        });
        return binding.getRoot();
    }

    private void init() {
        storageService = StorageService.getInstance(requireContext());
        if(storageService.getUser() == null) {
            getProfile();
        }
        disPlayUserInfo();
    }

    private void disPlayUserInfo() {
        User user = storageService.getUser();
        binding.userEmail.setText(user != null ? user.getEmail() : "");
        binding.userFirstname.setText(user != null ? user.getFirstName() : "");
        binding.userLastName.setText(user != null ? user.getLastName() : "");
        binding.fullName.setText(user != null ? user.getFullName() : "");
        displayUserAvatar();
    }

    private void displayUserAvatar() {
        User user = storageService.getUser();
        String avatarUrl = user.getAvatarUrl();
        Glide.with(requireContext())
                .load(avatarUrl.startsWith("https") ? avatarUrl:
                        (Constants.FILE_LOAD_ENDPOINT + avatarUrl))
                .centerCrop()
                .placeholder(R.drawable.ic_user_fill)
                .error(R.drawable.ic_user_fill)
                .into(binding.avatar);
    }

    private void getProfile() {
        Call<User> call = userApi.getProfile();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if(response.isSuccessful() && response.body() != null) {
                    storageService.setUser(response.body());
                    disPlayUserInfo();
                }
            }
            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Network error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
//        ProgressDialog progressDialog = new ProgressDialog(requireContext());
//        progressDialog.setMessage("Log out...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();

        Call<Void> call = userApi.logout();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
//                progressDialog.dismiss();
                if(response.isSuccessful()) {
                    storageService.setTokens(new AuthenticationResponse("", ""));
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
//                progressDialog.dismiss();
                Toast.makeText(requireContext(), "Network error!", Toast.LENGTH_SHORT).show();
            }
        });
    }


}