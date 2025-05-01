package com.group06.music_app_mobile.api_client;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit;


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
