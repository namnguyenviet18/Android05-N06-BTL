package com.group06.music_app_mobile.application.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.PlaylistApi;
import com.group06.music_app_mobile.api_client.api.SongApi;
import com.group06.music_app_mobile.app_utils.DownloadUtil;
import com.group06.music_app_mobile.app_utils.StorageService;
import com.group06.music_app_mobile.app_utils.enums.DataTransferBetweenScreens;
import com.group06.music_app_mobile.application.activities.PlayActivity;
import com.group06.music_app_mobile.application.adapters.HomeAdapter;
import com.group06.music_app_mobile.application.adapters.PlaylistDialogAdapter;
import com.group06.music_app_mobile.application.events.OnLongItemClickListener;
import com.group06.music_app_mobile.application.events.OnSongItemClickListener;
import com.group06.music_app_mobile.models.Playlist;
import com.group06.music_app_mobile.models.Song;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements OnSongItemClickListener, OnLongItemClickListener {
    private static final String TAG = "HomeFragment"; // Tag cho Log
    private RecyclerView recyclerRecent;
    private HomeAdapter adapter;
    private List<Song> songList;
    private DownloadUtil downloadUtil; // Thêm DownloadUtil
    private PlaylistApi playlistApi;
    private SongApi songApi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerRecent = view.findViewById(R.id.recyclerRecent);
        View seeAllTextView = view.findViewById(R.id.seeAllTextView);
        View seeAllTextView2 = view.findViewById(R.id.seeAllTextView2);

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

        seeAllTextView2.setOnClickListener(v -> {
            // Replace vào fragment_container thay vì nav_home
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AllPlaylistsFragment())
                    .addToBackStack(null)
                    .commit();

            // Ẩn ViewPager2 và BottomNavigationView
            requireActivity().findViewById(R.id.viewPager).setVisibility(View.GONE);
            requireActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);

            // Hiện FrameLayout chứa AllSongsFragment
            requireActivity().findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        });

        // Khởi tạo API Playlist
        playlistApi = ApiClient.getClient(getContext()).create(PlaylistApi.class);
        songApi = ApiClient.getClient(getContext()).create(SongApi.class);

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
                downloadSong(song);
            }
            else if(item.getItemId() == R.id.add_to_playlist_item) {
                fetchPlaylistsForDialog(song);
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


    private void fetchPlaylistsForDialog(Song song) {
        String authToken = StorageService.getInstance(requireContext()).getAccessToken();
        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(requireContext(), "Please login to continue", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<List<Playlist>> call = playlistApi.getUserPlaylists(authToken);
        call.enqueue(new Callback<List<Playlist>>() {
            @Override
            public void onResponse(Call<List<Playlist>> call, Response<List<Playlist>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Playlist> playlists = response.body();
                    showPlaylistDialog(playlists, song);
                } else {
                    Toast.makeText(getContext(), "Error loading playlists: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Playlist>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch playlists: " + t.getMessage());
                Toast.makeText(getContext(), "Connection error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPlaylistDialog(List<Playlist> playlists, Song song) {
        PlaylistDialogAdapter adapter = new PlaylistDialogAdapter(requireContext(), playlists);

        LinearLayout dialogLayout = new LinearLayout(requireContext());
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(0, 0, 0, 0);

        View titleView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_title_layout, null);
        dialogLayout.addView(titleView);

        View divider = new View(requireContext());
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        );
        dividerParams.setMargins(16, 0, 16, 0);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(Color.WHITE);
        dialogLayout.addView(divider);

        ListView listView = new ListView(requireContext());
        listView.setAdapter(adapter);
        listView.setPadding(16, 8, 16, 100);
        dialogLayout.addView(listView);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialog);
        builder.setView(dialogLayout);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Add click listener for playlist selection
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Playlist selectedPlaylist = playlists.get(position);
            addSongToPlaylist(selectedPlaylist, song);
            dialog.dismiss();
        });

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM;
            params.y = 20;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenHeight = displayMetrics.heightPixels;
            params.height = (int) (screenHeight * 2.0 / 3.0);
            window.setAttributes(params);

            listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            int maxItemsToShow = 5;
            float itemHeight = 100 * displayMetrics.density;
            int maxHeight = (int) (maxItemsToShow * itemHeight);
            ViewGroup.LayoutParams listParams = listView.getLayoutParams();
            listParams.height = maxHeight;
            listView.setLayoutParams(listParams);
        }

        TextView addPlaylistButton = titleView.findViewById(R.id.add_playlist_button);
        addPlaylistButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Add new playlist clicked!", Toast.LENGTH_SHORT).show();
            // TODO: Add logic to create new playlist
        });
    }

    private void addSongToPlaylist(Playlist playlist, Song song) {
        String authToken = StorageService.getInstance(requireContext()).getAccessToken();
        Call<ResponseBody> call = playlistApi.toggleSongInPlaylist(
                playlist.getId(), song.getId(), "Bearer " + authToken
        );

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseText = response.body().string().trim();

                        if ("Song added to playlist".equals(responseText)) {
                            Toast.makeText(getContext(), "🎵 Đã thêm bài hát vào playlist", Toast.LENGTH_SHORT).show();
                        } else if ("Song removed from playlist".equals(responseText)) {
                            Toast.makeText(getContext(), "🗑️ Đã xóa bài hát khỏi playlist", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "❓ Không rõ phản hồi: " + responseText, Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Lỗi đọc phản hồi", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi API: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Failed to toggle song in playlist: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchSongs();
    }
}