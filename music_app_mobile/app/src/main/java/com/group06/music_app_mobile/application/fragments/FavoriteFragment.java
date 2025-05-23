package com.group06.music_app_mobile.application.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.SongApi;
import com.group06.music_app_mobile.app_utils.DownloadUtil;
import com.group06.music_app_mobile.app_utils.ServerDestination;
import com.group06.music_app_mobile.app_utils.enums.DataTransferBetweenScreens;
import com.group06.music_app_mobile.application.activities.PlayActivity;
import com.group06.music_app_mobile.application.adapters.SongAdapter;
import com.group06.music_app_mobile.models.Song;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteFragment extends Fragment {
    private SongAdapter songAdapter;
    private List<Song> songList;
    private SongApi songApi;
    private ImageView coverImageView;
    private ImageView moreButton;
    private DownloadUtil downloadUtil;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        // Khởi tạo API
        songApi = ApiClient.getClient(getContext()).create(SongApi.class);

        // Khởi tạo ImageView lớn
        coverImageView = view.findViewById(R.id.coverImageView);

        // Khởi tạo nút More
        moreButton = view.findViewById(R.id.moreButton);

        // Khởi tạo DownloadUtil
        downloadUtil = new DownloadUtil(getContext());

        // Khởi tạo RecyclerView
        RecyclerView recyclerViewSongs = view.findViewById(R.id.recyclerViewSongs);
        recyclerViewSongs.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo danh sách và adapter
        songList = new ArrayList<>();
        songAdapter = new SongAdapter(getContext(), songList, this::updateCoverImage, this::downloadSelectedSong, this::toPlayActivity); // Thêm callback cho play
        recyclerViewSongs.setAdapter(songAdapter);

        // Xử lý nhấn nút More
        moreButton.setOnClickListener(v -> showPopupMenu(v));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
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
                    if (!songList.isEmpty()) {
                        updateCoverImage(songList.get(0));
                    }
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

    private void updateCoverImage(Song song) {
        String coverImageUrl = song.getCoverImageUrl();
        if (coverImageUrl != null && !coverImageUrl.isEmpty()) {
            String fullImageUrl = "http://" + ServerDestination.SERVER_HOST + ":" + ServerDestination.SERVER_PORT + "/api/v1/song/file/load?fullUrl=" + coverImageUrl;
            Glide.with(this)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.meha)
                    .error(R.drawable.meha)
                    .into(coverImageView);
        } else {
            coverImageView.setImageResource(R.drawable.meha);
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view, 0, 0, R.style.CustomPopupMenu);
        popupMenu.getMenu().add("Download");
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Download")) {
                downloadSelectedSong();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void downloadSelectedSong() {
        int selectedPosition = songAdapter.getSelectedPosition();
        if (selectedPosition != -1 && selectedPosition < songList.size()) {
            Song selectedSong = songList.get(selectedPosition);
            boolean success = downloadUtil.downloadSongById(getContext(), selectedSong.getId());
            if (success) {
                Toast.makeText(getContext(), "Đang tải bài hát: " + selectedSong.getName(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Tải bài hát thất bại!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Vui lòng chọn một bài hát để tải!", Toast.LENGTH_SHORT).show();
        }
    }

    // Phương thức để chuyển sang PlayActivity
    public void toPlayActivity(int currentSongPosition) {
        Intent intent = new Intent(requireActivity(), PlayActivity.class);
        intent.putExtra(DataTransferBetweenScreens.SONG_LIST.name(), (Serializable) songList);
        intent.putExtra(DataTransferBetweenScreens.CURRENT_SONG_POSITION.name(), currentSongPosition);
        startActivity(intent);
    }
}