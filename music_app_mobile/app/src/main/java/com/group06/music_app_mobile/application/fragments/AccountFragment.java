package com.group06.music_app_mobile.application.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.AuthApi;
import com.group06.music_app_mobile.api_client.requests.GoogleAuthRequest;
import com.group06.music_app_mobile.api_client.responses.AuthenticationResponse;
import com.group06.music_app_mobile.api_client.responses.ExceptionResponse;
import com.group06.music_app_mobile.app_utils.Constants;
import com.group06.music_app_mobile.application.activities.PlayActivity;
import com.group06.music_app_mobile.databinding.FragmentAccountBinding;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;

    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private AuthApi authApi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        binding.accountName.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), PlayActivity.class);
            startActivity(intent);
        });

//        authApi = ApiClient.getClient(requireContext()).create(AuthApi.class);
//
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(Constants.GOOGLE_CLIENT_ID)
//                .requestEmail()
//                .build();
//
//        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
//
//        googleSignInLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == RESULT_OK) {
//                        Intent data = result.getData();
//                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//                        handleSignInResult(task);
//                    } else {
//                        Toast.makeText(requireContext(), "Google Sign-In canceled", Toast.LENGTH_SHORT).show();
//                    }
//                }
//        );
//
//        binding.accountEmail.setOnClickListener(view -> {
//            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
//                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//                googleSignInLauncher.launch(signInIntent);
//            });
//        });
        return binding.getRoot();

    }

//    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
//        try {
//            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
//            String idToken = account.getIdToken();
//            String email = account.getEmail();
//            String name = account.getDisplayName();
//
//            // Log hoặc gửi idToken lên backend
//            Log.d("TOKEN ID", "ID Token: " + idToken);
//            Toast.makeText(requireContext(), "Đăng nhập thành công: " + email, Toast.LENGTH_SHORT).show();
//
//            Call<AuthenticationResponse> call = authApi.authenticateWithGoogle(GoogleAuthRequest.builder()
//                    .tokenId(idToken)
//                    .build());
//
//            call.enqueue(new Callback<AuthenticationResponse>() {
//                @Override
//                public void onResponse(Call<AuthenticationResponse> call, Response<AuthenticationResponse> response) {
//                    if(response.code() == 200) {
//                        Log.d("TOKEN", "accessToken: " + response.body().getAccessToken());
//                        if(response.body() != null)  return;
//                        Toast.makeText(requireContext(), response.body().getAccessToken(), Toast.LENGTH_SHORT).show();
//                    } else if(response.code() == 400) {
//                        String errorJson = null;
//                        try {
//                            errorJson = response.errorBody().string();
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                        // Dùng Gson parse về ExceptionResponse
//                        Gson gson = new Gson();
//                        ExceptionResponse errorResponse = gson.fromJson(errorJson, ExceptionResponse.class);
//
//                        // Hiển thị message từ backend
//                        Toast.makeText(requireContext(), errorResponse.getMessage(), Toast.LENGTH_SHORT).show();
//
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<AuthenticationResponse> call, Throwable t) {
//
//                }
//            });
//
//            // TODO: Gửi idToken lên Spring Boot backend để xác thực và nhận accessToken
//        } catch (ApiException e) {
//            Log.w("TOKEN ERR", "signInResult:failed code=" + e.getStatusCode());
//            Toast.makeText(requireContext(), "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
//        }
//    }
}