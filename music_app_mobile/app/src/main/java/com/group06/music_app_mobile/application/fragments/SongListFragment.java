package com.group06.music_app_mobile.application.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.responses.SongResponse;
import com.group06.music_app_mobile.app_utils.ServerDestination;
import com.group06.music_app_mobile.application.adapters.SongResponseAdapter;
import com.group06.music_app_mobile.app_utils.StorageService;
import com.group06.music_app_mobile.application.activities.PlayActivity;
import com.group06.music_app_mobile.application.events.OnSongItemClickListener;
import com.group06.music_app_mobile.models.Song;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SongListFragment extends Fragment implements OnSongItemClickListener {
    private static final String TAG = "SongListFragment";
    private RecyclerView recyclerSongs;
    private SongResponseAdapter adapter;
    private List<Song> songList;
    private List<Song> originalSongList;
    private EditText searchInput;
    private String playlistId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
                requireActivity().findViewById(R.id.viewPager).setVisibility(View.VISIBLE);
                requireActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.VISIBLE);
                requireActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        if (getArguments() != null) {
            playlistId = getArguments().getString("playlist_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_songs, container, false);
        recyclerSongs = view.findViewById(R.id.recyclerAllSongs);
        searchInput = view.findViewById(R.id.searchInput);
        ImageView backButton = view.findViewById(R.id.backButton);

        songList = new ArrayList<>();
        originalSongList = new ArrayList<>();
        adapter = new SongResponseAdapter(requireContext(), songList);
        recyclerSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerSongs.setAdapter(adapter);

        adapter.setOnItemClickListener(this);

        searchInput.setEnabled(false);

        fetchSongs();

        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
            requireActivity().findViewById(R.id.viewPager).setVisibility(View.VISIBLE);
            requireActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.VISIBLE);
            requireActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filterSongs(s.toString());
            }
        });

        return view;
    }

    private void fetchSongs() {
        String authToken = StorageService.getInstance(requireContext()).getAccessToken();
        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .protocols(Arrays.asList(Protocol.HTTP_1_1, Protocol.HTTP_2))
                .build();

        String url = "http://" + ServerDestination.SERVER_HOST + ":" + ServerDestination.SERVER_PORT + "/api/v1/playlist/" + playlistId + "/songs/user";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + authToken)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() -> {
                    Log.e(TAG, "Lỗi kết nối: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                String jsonResponse;
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    jsonResponse = responseBody.string();
                    Log.d(TAG, "JSON Response: " + jsonResponse);
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Lỗi: Không có dữ liệu trả về từ server");
                        Toast.makeText(requireContext(), "Lỗi: Không có dữ liệu trả về từ server", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                List<Song> songs;
                try {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Song>>(){}.getType();
                    System.out.println(jsonResponse);
                    songs = gson.fromJson(jsonResponse, listType);
                    if (songs == null || songs.isEmpty()) {
                        requireActivity().runOnUiThread(() -> {
                            Log.e(TAG, "Danh sách bài hát rỗng hoặc null");
                            Toast.makeText(requireContext(), "Không có bài hát nào trong playlist", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Lỗi phân tích dữ liệu: " + e.getMessage(), e);
                        Toast.makeText(requireContext(), "Lỗi phân tích dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Dữ liệu nhận được, số bài hát: " + songs.size());
                        originalSongList.clear();
                        originalSongList.addAll(songs);
                        updateSongData();
                        searchInput.setEnabled(true);
                    } else {
                        Log.e(TAG, "Lỗi từ server: " + jsonResponse + " (Code: " + response.code() + ")");
                        Toast.makeText(requireContext(), "Lỗi: " + jsonResponse + " (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void updateSongData() {
        songList.clear();
        songList.addAll(originalSongList);
        Log.d(TAG, "Updating adapter with " + songList.size() + " songs");
        adapter.updateSongs(songList);
        if (songList.isEmpty()) {
            Toast.makeText(requireContext(), "Không có bài hát nào để hiển thị", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSongData(List<Song> filteredList) {
        songList.clear();
        if (filteredList != null) {
            songList.addAll(filteredList);
            Log.d(TAG, "Updating adapter with filtered list: " + songList.size() + " songs");
        } else {
            Log.e(TAG, "Filtered list is null");
        }
        adapter.updateSongs(songList);
    }

    private void filterSongs(String query) {
        List<Song> filteredList = new ArrayList<>();
        Log.d(TAG, "Filtering songs, original size: " + originalSongList.size());
        if (query.isEmpty()) {
            filteredList.addAll(originalSongList);
        } else {
            for (Song song : originalSongList) {
                if (song.getName() != null && song.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(song);
                }
            }
        }
        Log.d(TAG, "Filtered size: " + filteredList.size());
        updateSongData(filteredList);
    }

    @Override
    public void toPlayActivity(int currentSongPosition) {
        Log.d(TAG, "Opening PlayActivity with position: " + currentSongPosition + ", songList size: " + songList.size());
        Intent intent = new Intent(requireActivity(), PlayActivity.class);
        intent.putExtra("SONG_LIST", (java.io.Serializable) songList); // Truyền List<Song> thay vì List<SongResponse>
        intent.putExtra("CURRENT_SONG_POSITION", currentSongPosition);
        startActivity(intent);
    }

//    // Phương thức chuyển đổi SongResponse sang Song
//    private Song convertToSong(SongResponse songResponse) {
//        Song song = new Song(); // Giả định Song có constructor mặc định
//        song.setId(songResponse.getId());
//        song.setName(songResponse.getName());
//        song.setAuthorName(songResponse.getAuthorName());
//        song.setSingerName(songResponse.getSingerName());
//        song.setAudioUrl(songResponse.getAudioUrl());
//        song.setCoverImageUrl(songResponse.getCoverImageUrl());
//        song.setLyrics(songResponse.getLyrics());
//        song.setPublic(songResponse.isPublic());
//        song.setDuration(songResponse.getDuration() * 1000); // Chuyển từ giây sang mili giây
//        return song;
//    }
}