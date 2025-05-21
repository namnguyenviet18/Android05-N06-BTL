package com.group06.music_app_mobile.application.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.SongApi;
import com.group06.music_app_mobile.api_client.responses.SongResponse;
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

public class AllSongsFragment extends Fragment implements OnSongItemClickListener, OnLongItemClickListener {
    private static final String TAG = "AllSongsFragment";
    private RecyclerView recyclerRecent;
    private HomeAdapter adapter;
    private List<Song> songList;
    private List<Song> originalSongList; // Danh sách gốc để lọc
    private EditText searchInput;

    // Khởi tạo những thuộc tính mặc định của điện thoại
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Đăng ký OnBackPressedCallback để xử lý nút back của điện thoại khi vào màn hình AllSongs
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
        View view = inflater.inflate(R.layout.fragment_all_songs, container, false);
        recyclerRecent = view.findViewById(R.id.recyclerAllSongs);
        searchInput = view.findViewById(R.id.searchInput); // Lấy tham chiếu đến EditText
        View backButton = view.findViewById(R.id.backButton);

        // Khởi tạo Adapter với danh sách rỗng
        songList = new ArrayList<>();
        originalSongList = new ArrayList<>();
        adapter = new HomeAdapter(songList, this, this);
        recyclerRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerRecent.setAdapter(adapter);

        // Gọi API để lấy danh sách bài hát
        fetchSongs();

        // Xử lý khi nhấn nút Back
        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
            requireActivity().findViewById(R.id.viewPager).setVisibility(View.VISIBLE);
            requireActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.VISIBLE);
            requireActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
        });

        // Thêm TextWatcher cho searchInput
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Không cần xử lý
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterSongs(s.toString());
            }
        });

        return view;
    }

    private void fetchSongs() {
        Log.d(TAG, "Bắt đầu gọi API song/list");
        SongApi songApi = ApiClient.getClient(getContext()).create(SongApi.class);
        Call<List<Song>> call = songApi.getAllSongs();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                Log.d(TAG, "Nhận phản hồi từ API, mã trạng thái: " + response.code());
                int code = response.code();
                if (code == 200 && response.body() != null) {
                    Log.d(TAG, "Dữ liệu nhận được, số bài hát: " + response.body().size());
                    // Cập nhật danh sách gốc và danh sách hiển thị
                    originalSongList.clear();
                    originalSongList.addAll(response.body());
                    adapter.updateSongs(originalSongList);
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
                Log.e(TAG, "Lỗi kết nối: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterSongs(String query) {
        List<Song> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            // Nếu query rỗng, hiển thị toàn bộ danh sách gốc
            filteredList.addAll(originalSongList);
        } else {
            // Lọc danh sách dựa trên tên bài hát
            for (Song song : originalSongList) {
                if (song.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(song);
                }
            }
        }
        // Cập nhật Adapter với danh sách đã lọc
        adapter.updateSongs(filteredList);
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
            if(item.getItemId() == R.id.download_item) {
                Toast.makeText(getContext(), "Click download", Toast.LENGTH_SHORT).show();
            }else if(item.getItemId() == R.id.add_to_playlist_item) {
                Toast.makeText(getContext(), "Add to playlist", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        popupMenu.show();
    }
}