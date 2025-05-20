package com.group06.music_app_mobile.application.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.SongApi;
import com.group06.music_app_mobile.api_client.responses.SongResponse;
import com.group06.music_app_mobile.application.adapters.HomeAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment"; // Tag cho Log
    private RecyclerView recyclerRecent;
    private HomeAdapter adapter;
    private List<SongResponse> songList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerRecent = view.findViewById(R.id.recyclerRecent);
        View seeAllTextView = view.findViewById(R.id.seeAllTextView);

        // Khởi tạo Adapter với danh sách rỗng
        songList = new ArrayList<>();
        adapter = new HomeAdapter(songList);
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
        Call<List<SongResponse>> call = songApi.getAllSongs();

        // Gọi API bất đồng bộ
        call.enqueue(new Callback<List<SongResponse>>() {
            @Override
            public void onResponse(Call<List<SongResponse>> call, Response<List<SongResponse>> response) {
                Log.d(TAG, "Nhận phản hồi từ API, mã trạng thái: " + response.code());
                int code = response.code();
                if (code == 200 && response.body() != null) {
                    Log.d(TAG, "Dữ liệu nhận được, số bài hát: " + response.body().size());
                    // Cập nhật Adapter với danh sách bài hát
                    adapter.updateSongs(response.body());
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
            public void onFailure(Call<List<SongResponse>> call, Throwable t) {
                Log.e(TAG, "Lỗi kết nối: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}