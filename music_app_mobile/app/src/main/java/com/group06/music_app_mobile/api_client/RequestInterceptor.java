package com.group06.music_app_mobile.api_client;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.group06.music_app_mobile.api_client.api.AuthApi;
import com.group06.music_app_mobile.api_client.callbacks.RefreshTokenCallBack;
import com.group06.music_app_mobile.api_client.responses.AuthenticationResponse;
import com.group06.music_app_mobile.app_utils.StorageService;
import com.group06.music_app_mobile.application.activities.LoginActivity;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class RequestInterceptor implements Interceptor {

    private final StorageService storageService;
    private final Context context;

    public RequestInterceptor(Context context) {
        this.context = context;
        storageService = StorageService.getInstance(this.context);
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Log.d("INTER", "VAO INTERCEPTOR");
        Request originalRequest = chain.request();
        String path = originalRequest.url().encodedPath();
        Request.Builder builder = originalRequest.newBuilder();

        if (!path.contains("auth")) {
            builder.header("Authorization", "Bearer " + storageService.getAccessToken());
        }

        if (path.contains("logout")) {
            builder.header("REFRESH_TOKEN", "Bearer " + storageService.getRefreshToken());
        }

        originalRequest = builder.build();
        Response response = chain.proceed(originalRequest);
        if(response.code() == 401) {
            response.close();
            boolean refreshResult = refreshToken(storageService.getRefreshToken());
            if(refreshResult) {
                Request newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + storageService.getAccessToken())
                        .build();
                return chain.proceed(newRequest);
            }else {
                logout();
            }
        }
        return response;
    }

    private boolean refreshToken(String refreshToken) {
        try {
            AuthApi authApi = ApiClient.getClient(context).create(AuthApi.class);
            Call<AuthenticationResponse> call = authApi.refreshToken(refreshToken);
            retrofit2.Response<AuthenticationResponse> response = call.execute();
            if(response.isSuccessful() && response.body() != null) {
                storageService.setAccessToken(response.body().getAccessToken());
                return true;
            }
        }catch (Exception exp) {
            System.err.println(exp.getMessage());
        }
        return false;
    }

    private void logout() {
        storageService.clearAll();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
