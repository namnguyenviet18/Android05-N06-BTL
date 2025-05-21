package com.group06.music_app_mobile.app_utils;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.group06.music_app_mobile.application.activities.MainActivity;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoogleAuthService {

    private static GoogleAuthService instance;

    @SuppressLint("StaticFieldLeak")
    private static GoogleSignInClient mGoogleSignInClient;
    private static ActivityResultLauncher<Intent> googleSignInLauncher;

    private GoogleAuthService() {}

    public static GoogleAuthService getInstance(AppCompatActivity activity, boolean isRefresh) {
        if(instance == null) {
            synchronized (GoogleAuthService.class) {
                instance = new GoogleAuthService();
                instance.initGoogleSignIn(activity);
            }
        }
        if(isRefresh) {
            instance.initGoogleSignIn(activity);
        }
        return instance;
    }

    private void initGoogleSignIn(AppCompatActivity activity) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.GOOGLE_CLIENT_ID)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
        googleSignInLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task, activity);
                    } else {
                        Toast.makeText(activity, "Google Sign-In canceled", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
    public void signIn() {
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private static void handleSignInResult(Task<GoogleSignInAccount> completedTask, AppCompatActivity activity) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String tokenId = account.getIdToken();
            if(tokenId == null) {
                Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("TOKEN ID", "ID Token: " + tokenId);
            verifyTokenId(tokenId, activity);
        } catch (ApiException e) {
            Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private static void verifyTokenId(String tokenId, AppCompatActivity activity) {
        AuthApi authApi = ApiClient.getClient(activity).create(AuthApi.class);
        Call<AuthenticationResponse> call = authApi.authenticateWithGoogle(GoogleAuthRequest.builder()
                .tokenId(tokenId)
                .build());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<AuthenticationResponse> call, @NonNull Response<AuthenticationResponse> response) {
                if (response.code() == 200) {
                    Log.d("ACCESS TOKEN", response.body().getAccessToken());
                    Log.d("REFRESH TOKEN", response.body().getRefreshToken());
                    StorageService.getInstance(activity).setTokens(response.body());
                    Intent intent = new Intent(activity, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    activity.startActivity(intent);
                } else if (response.code() == 400) {
                    String errorJson = null;
                    try {
                        errorJson = response.errorBody().string();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Gson gson = new Gson();
                    ExceptionResponse errorResponse = gson.fromJson(errorJson, ExceptionResponse.class);
                    Toast.makeText(activity, errorResponse.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthenticationResponse> call, @NonNull Throwable t) {
                Toast.makeText(activity, "Network error!", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
