package com.group06.music_app_mobile.application.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.AuthApi;
import com.group06.music_app_mobile.api_client.requests.AuthenticationRequest;
import com.group06.music_app_mobile.api_client.responses.AuthenticationResponse;
import com.group06.music_app_mobile.app_utils.Constants;
import com.group06.music_app_mobile.application.activities.PlayActivity;
import com.group06.music_app_mobile.databinding.FragmentPlayBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlayFragment extends Fragment {

    private FragmentPlayBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPlayBinding.inflate(inflater, container, false);
        binding.buttonPlay.setOnClickListener(view -> {
            Intent intent = new Intent(requireActivity(), PlayActivity.class);
            startActivity(intent);
//            Log.d("DEBUG", Constants.SERVER_DOMAIN);
//            AuthApi authApi = ApiClient.getClient(requireContext()).create(AuthApi.class);
//            Call<AuthenticationResponse> call = authApi.authenticate(AuthenticationRequest.builder()
//                    .email("nam@gmail.com")
//                    .password("123456789")
//                    .build());
//            call.enqueue(new Callback<>() {
//                @Override
//                public void onResponse(Call<AuthenticationResponse> call, Response<AuthenticationResponse> response) {
//                    Log.d("TEST", "Vao day");
//                    assert response.body() != null;
//                    Log.d("TEST", response.body().getAccessToken());
//                }
//
//                @Override
//                public void onFailure(Call<AuthenticationResponse> call, Throwable t) {
//                    Log.e("AUTH", "API call failed", t);
//                    Toast.makeText(requireContext(), "API error: " + t.getMessage(), Toast.LENGTH_LONG).show();
//                }
//            });
        });
        return binding.getRoot();
    }
}