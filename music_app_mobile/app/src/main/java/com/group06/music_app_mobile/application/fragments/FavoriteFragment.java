package com.group06.music_app_mobile.application.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.SongApi;
import com.group06.music_app_mobile.application.adapters.SongAdapter;
import com.group06.music_app_mobile.models.Song;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteFragment extends Fragment {

    private CardView likeButtonContainer;
    private ImageView likeButton;
    private boolean isLiked = false; // Trạng thái của nút Like tổng
    private SongAdapter songAdapter;
    private List<Song> songList;
    private SongApi songApi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        // Khởi tạo API
        songApi = ApiClient.getClient(getContext()).create(SongApi.class);

        // Khởi tạo nút Like tổng
        likeButtonContainer = view.findViewById(R.id.likeButtonContainer);
        likeButton = view.findViewById(R.id.likeButton);

        // Xử lý sự kiện click cho nút Like tổng
        likeButtonContainer.setOnClickListener(v -> {
            isLiked = !isLiked;
            View likeButtonBackground = likeButtonContainer.findViewById(R.id.likeButtonBackground);
            if (isLiked) {
                likeButtonBackground.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.gradient_background));
                likeButton.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.text_white)));
            } else {
                likeButtonBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.bg_gray));
                likeButton.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.cyan_light)));
            }
        });

        // Khởi tạo RecyclerView
        RecyclerView recyclerViewSongs = view.findViewById(R.id.recyclerViewSongs);
        recyclerViewSongs.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo danh sách và adapter
        songList = new ArrayList<>();
        songAdapter = new SongAdapter(getContext(), songList);
        recyclerViewSongs.setAdapter(songAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Gọi API mỗi khi Fragment quay lại
        fetchLikedSongs();
    }

    private void fetchLikedSongs() {
        Call<List<Song>> call = songApi.getLikedSong();
        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    songList.clear();
                    songList.addAll(response.body());
                    songAdapter.updateSongs(songList);
                    if (songList.isEmpty()) {
                        Toast.makeText(getContext(), "Bạn chưa thích bài hát nào!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi khi lấy danh sách: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}