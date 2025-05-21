package com.group06.music_app_mobile.application.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.SongApi;
import com.group06.music_app_mobile.api_client.responses.SongResponse;
import com.group06.music_app_mobile.app_utils.DownloadUtil;
import com.group06.music_app_mobile.app_utils.enums.DataTransferBetweenScreens;
import com.group06.music_app_mobile.application.activities.PlayActivity;
import com.group06.music_app_mobile.application.adapters.HomeAdapter;
import com.group06.music_app_mobile.application.events.OnLongItemClickListener;
import com.group06.music_app_mobile.application.events.OnSongItemClickListener;
import com.group06.music_app_mobile.models.Song;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements OnSongItemClickListener, OnLongItemClickListener {
    private static final String TAG = "HomeFragment"; // Tag cho Log
    private RecyclerView recyclerRecent;
    private HomeAdapter adapter;
    private List<Song> songList;
    private DownloadUtil downloadUtil; // Thêm DownloadUtil

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerRecent = view.findViewById(R.id.recyclerRecent);
        View seeAllTextView = view.findViewById(R.id.seeAllTextView);

        // Khởi tạo Adapter với danh sách rỗng
        songList = new ArrayList<>();
        // Khởi tạo DownloadUtil
        downloadUtil = new DownloadUtil(requireContext());
        adapter = new HomeAdapter(songList, this, this);
        recyclerRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerRecent.setAdapter(adapter);

        // Gọi API để lấy danh sách bài hát
        fetchSongs();

        seeAllTextView.setOnClickListener(v -> {
            // Replace vào fragment_container thay vì nav_home
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AllSongsFragment())
                    .addToBackStack(null)
                    .commit();

            // Ẩn ViewPager2 và BottomNavigationView
            requireActivity().findViewById(R.id.viewPager).setVisibility(View.GONE);
            requireActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);

            // Hiện FrameLayout chứa AllSongsFragment
            requireActivity().findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        });

        return view;
    }

    private void fetchSongs() {
        Log.d(TAG, "Bắt đầu gọi API song/list");

        // Khởi tạo SongApi bằng ApiClient
        SongApi songApi = ApiClient.getClient(getContext()).create(SongApi.class);
        Call<List<Song>> call = songApi.getAllSongs();

        // Gọi API bất đồng bộ
        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                Log.d(TAG, "Nhận phản hồi từ API, mã trạng thái: " + response.code());
                int code = response.code();
                if (code == 200 && response.body() != null) {
                    Log.d(TAG, "Dữ liệu nhận được, số bài hát: " + response.body().size());
                    songList = response.body();
                    adapter.updateSongs(songList);
                } else {
                    String errorMessage = switch (code) {
                        case 400 -> "Yêu cầu không hợp lệ";
                        case 404 -> "Không tìm thấy bài hát";
                        default -> "Lỗi khi lấy danh sách bài hát: " + code;
                    };
                    Log.e(TAG, "Lỗi từ server: " + errorMessage);
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {

            }
        });
    }

    @Override
    public void toPlayActivity(int currentSongPosition) {
        Intent intent = new Intent(requireActivity(), PlayActivity.class);
        intent.putExtra(DataTransferBetweenScreens.SONG_LIST.name(), (Serializable) songList);
        intent.putExtra(DataTransferBetweenScreens.CURRENT_SONG_POSITION.name(), currentSongPosition);
        startActivity(intent);
    }


    @Override
    public void onLongItemClick(Song song, View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_song_item, popupMenu.getMenu());
        for (int i = 0; i < popupMenu.getMenu().size(); i++) {
            MenuItem item = popupMenu.getMenu().getItem(i);
            SpannableString spanString = new SpannableString(item.getTitle());
            spanString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spanString.length(), 0);
            item.setTitle(spanString);
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.download_item) {
//                if (checkStoragePermissions()) {
//                    downloadSong(song);
//                } else {
//                    requestStoragePermissions();
//                }
                downloadSong(song);}
            else if(item.getItemId() == R.id.add_to_playlist_item) {
                Toast.makeText(getContext(), "Add to playlist", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        popupMenu.show();
    }
    private void downloadSong(Song song) {
        long songId = song.getId();
        boolean success = downloadUtil.downloadSongById(requireContext(), songId);
        if (success) {
            Toast.makeText(getContext(), "Đã tải bài hát: " + song.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Tải bài hát thất bại: " + song.getName(), Toast.LENGTH_SHORT).show();
        }
    }
}