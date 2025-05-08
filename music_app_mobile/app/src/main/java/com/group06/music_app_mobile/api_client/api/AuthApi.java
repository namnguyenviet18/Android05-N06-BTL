package com.group06.music_app_mobile.api_client.api;

import com.group06.music_app_mobile.api_client.requests.AuthenticationRequest;
import com.group06.music_app_mobile.api_client.requests.RegisterRequest;
import com.group06.music_app_mobile.api_client.requests.VerifyOtpRequest;
import com.group06.music_app_mobile.api_client.responses.AuthenticationResponse;
import com.group06.music_app_mobile.models.OTP;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AuthApi {

    @POST("auth/register")
    Call<OTP> register(@Body RegisterRequest request);

    @POST("auth/authenticate")
    Call<AuthenticationResponse> authenticate(@Body AuthenticationRequest request);

    @POST("auth/verify-otp")
    Call<AuthenticationResponse> verifyOtp(@Body VerifyOtpRequest request);

    @GET("auth/resend-otp/{email}")
    Call<OTP> resendOtp(@Path("email") String email);

    @GET("auth/refresh-token")
    Call<AuthenticationResponse> refreshToken(
            @Header("Authorization") String refreshToken
    );
}