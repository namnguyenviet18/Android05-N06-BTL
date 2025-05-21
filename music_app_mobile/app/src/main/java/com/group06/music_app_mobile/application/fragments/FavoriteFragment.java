package com.group06.music_app_mobile.application.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.application.adapters.SongAdapter;
import com.group06.music_app_mobile.models.Song;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {

    private CardView likeButtonContainer;
    private ImageView likeButton;
    private boolean isLiked = false; // Trạng thái của nút Like

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        // Khởi tạo nút Like
        likeButtonContainer = view.findViewById(R.id.likeButtonContainer);
        likeButton = view.findViewById(R.id.likeButton);

        // Xử lý sự kiện click cho nút Like
        likeButtonContainer.setOnClickListener(v -> {
            isLiked = !isLiked;

            // Tìm View con để đặt gradient/màu nền
            View likeButtonBackground = likeButtonContainer.findViewById(R.id.likeButtonBackground);

            if (isLiked) {
                // Khi bấm: Màu nền là gradient, icon trắng
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

        // Tạo danh sách bài hát giả lập sử dụng model Song
        List<Song> songList = new ArrayList<>();
//        songList.add(new Song("Dailamo Dailamo", "Sangeetha Rajeshwaran", "Vijay Antony", "", "", "", "", "", 0, 0, null, null, true, false, false, null, 0));
//        songList.add(new Song("Saara Kaattrae", "S. P. Balasubrahmanyam", "", "", "", "", "", "", 0, 0, null, null, true, false, false, null, 0));
//        songList.add(new Song("Marundhaani", "Nakash Aziz", "Anthony Daasan", "", "", "", "", "", 0, 0, null, null, true, false, false, null, 0));
//        songList.add(new Song("Oru Devadhai", "Roopkumar Rathod", "", "", "", "", "", "", 0, 0, null, null, true, false, false, null, 0));
//        songList.add(new Song("Marundhaani", "Nakash Aziz", "Anthony Daasan", "", "", "", "", "", 0, 0, null, null, true, false, false, null, 0));

        // Khởi tạo adapter và gán cho RecyclerView
        SongAdapter songAdapter = new SongAdapter(getContext(), songList);
        recyclerViewSongs.setAdapter(songAdapter);

        return view;
    }
}