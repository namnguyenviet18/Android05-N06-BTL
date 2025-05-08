package com.group06.music_app_mobile.api_client;

import android.content.Context;

import com.group06.music_app_mobile.app_utils.Constants;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit;

    public static Retrofit getClient(Context context) {
        if(retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new RequestInterceptor(context))
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.SERVER_DOMAIN)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }


    /// TEST
    public static Retrofit getClientCloudinary() {
        if(retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://res.cloudinary.com")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
