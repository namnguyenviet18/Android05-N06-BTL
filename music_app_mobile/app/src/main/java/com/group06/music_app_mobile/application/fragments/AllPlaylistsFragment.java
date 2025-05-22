package com.group06.music_app_mobile.application.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.responses.PlaylistResponse;
import com.group06.music_app_mobile.app_utils.ServerDestination;
import com.group06.music_app_mobile.application.adapters.PlaylistAdapter;
import com.group06.music_app_mobile.app_utils.StorageService;
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

public class AllPlaylistsFragment extends Fragment {
    private static final String TAG = "AllPlaylistsFragment";
    private RecyclerView recyclerPlaylists;
    private PlaylistAdapter adapter;
    private List<PlaylistResponse> playlistList;
    private List<PlaylistResponse> originalPlaylistList;
    private EditText searchInput;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_playlists, container, false);
        recyclerPlaylists = view.findViewById(R.id.recyclerAllPlaylists);
        searchInput = view.findViewById(R.id.searchInput);
        View backButton = view.findViewById(R.id.backButton);

        playlistList = new ArrayList<>();
        originalPlaylistList = new ArrayList<>();
        adapter = new PlaylistAdapter(requireContext(), playlistList);
        recyclerPlaylists.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPlaylists.setAdapter(adapter);
        searchInput.setEnabled(false);

        // Set click listener for playlist items
        adapter.setOnItemClickListener(playlist -> {
            SongListFragment songListFragment = new SongListFragment();
            Bundle bundle = new Bundle();
            bundle.putString("playlist_id", playlist.getId().toString()); // Assume PlaylistResponse has getId()
            songListFragment.setArguments(bundle);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, songListFragment)
                    .addToBackStack(null)
                    .commit();
            requireActivity().findViewById(R.id.viewPager).setVisibility(View.GONE);
            requireActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);
            requireActivity().findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        });

        fetchPlaylists();

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
                filterPlaylists(s.toString());
            }
        });

        return view;
    }

    private void fetchPlaylists() {
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

        String url = "http://" + ServerDestination.SERVER_HOST + ":" + ServerDestination.SERVER_PORT + "/api/v1/playlist/user";
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

                List<PlaylistResponse> playlists;
                try {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<PlaylistResponse>>(){}.getType();
                    playlists = gson.fromJson(jsonResponse, listType);
                    if (playlists == null || playlists.isEmpty()) {
                        requireActivity().runOnUiThread(() -> {
                            Log.e(TAG, "Danh sách playlist rỗng hoặc null");
                            Toast.makeText(requireContext(), "Không có playlist nào từ server", Toast.LENGTH_SHORT).show();
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
                        Log.d(TAG, "Dữ liệu nhận được, số playlist: " + playlists.size());
                        originalPlaylistList.clear();
                        originalPlaylistList.addAll(playlists);
                        updatePlaylistData(playlists);
                        searchInput.setEnabled(true);
                    } else {
                        Log.e(TAG, "Lỗi từ server: " + jsonResponse + " (Code: " + response.code() + ")");
                        Toast.makeText(requireContext(), "Lỗi: " + jsonResponse + " (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

//    private void updatePlaylistData() {
//        playlistList.clear();
//        playlistList.addAll(originalPlaylistList);
//        Log.d(TAG, "Updating adapter with " + playlistList.size() + " playlists");
//        adapter.updatePlaylists(playlistList);
//        if (playlistList.isEmpty()) {
//            Toast.makeText(requireContext(), "Không có playlist nào để hiển thị", Toast.LENGTH_SHORT).show();
//        }
//    }

    private void updatePlaylistData(List<PlaylistResponse> filteredList) {

        playlistList.clear();
        if (filteredList != null) {
            playlistList.addAll(filteredList);
            Log.d(TAG, "Updating adapter with filtered list: " + playlistList.size() + " playlists");
        } else {
            Log.e(TAG, "Filtered list is null");
        }
        System.out.println("Playlist before "+playlistList);
        adapter.updatePlaylists(playlistList);
    }

    private void filterPlaylists(String query) {
        List<PlaylistResponse> filteredList = new ArrayList<>();
        Log.d(TAG, "Filtering playlists, original size: " + originalPlaylistList.size());
        if (query.isEmpty()) {
            filteredList.addAll(originalPlaylistList);
        } else {
            for (PlaylistResponse playlist : originalPlaylistList) {
                if (playlist.getName() != null && playlist.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(playlist);
                }
            }
        }
        Log.d(TAG, "Filtered size: " + filteredList.size());
        updatePlaylistData(filteredList);
    }
}