package com.group06.music_app_mobile.api_client.api;

import com.group06.music_app_mobile.api_client.requests.ChangePasswordRequest;
import com.group06.music_app_mobile.models.OTP;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;

public interface UserApi {
    @PATCH("user/change-password")
    Call<Void> changePassword(@Body ChangePasswordRequest request);
}
