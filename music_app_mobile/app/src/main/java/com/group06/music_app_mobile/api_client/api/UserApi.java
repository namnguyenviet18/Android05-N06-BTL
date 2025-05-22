package com.group06.music_app_mobile.api_client.api;

import com.group06.music_app_mobile.api_client.requests.ChangePasswordRequest;
import com.group06.music_app_mobile.api_client.requests.EditProfileRequest;
import com.group06.music_app_mobile.models.OTP;
import com.group06.music_app_mobile.models.User;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UserApi {
    @PATCH("user/change-password")
    Call<Void> changePassword(@Body ChangePasswordRequest request);

    @GET("user/profile")
    Call<User> getProfile();

    @PATCH("user/update-profile")
    Call<User> updateProfile(@Body EditProfileRequest request);



    @POST("user/logout")
    Call<Void> logout();

    @Multipart
    @PATCH("user/change-avatar")
    Call<User> changeAvatar(
            @Header("Authorization") String authorization,
            @Part MultipartBody.Part avatar
    );

}
