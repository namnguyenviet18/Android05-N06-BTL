package com.group06.music_app_mobile.application.fragments;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.LoadFileApi;
import com.group06.music_app_mobile.app_utils.Constants;
import com.group06.music_app_mobile.application.activities.PlayActivity;
import com.group06.music_app_mobile.application.adapters.LyricAdapter;
import com.group06.music_app_mobile.application.events.OnLyricItemClickListener;
import com.group06.music_app_mobile.databinding.FragmentLyricBinding;
import com.group06.music_app_mobile.models.LyricLine;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LyricFragment extends Fragment implements OnLyricItemClickListener {

    private List<LyricLine> lyrics;
    private LyricAdapter lyricAdapter;
    private FragmentLyricBinding binding;

    private boolean isPlayDownloadedSong;

    public LyricFragment(boolean isPlayDownloadedSong) {
        this.isPlayDownloadedSong = isPlayDownloadedSong;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLyricBinding.inflate(inflater, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.lyricRecycler.setLayoutManager(layoutManager);
        lyrics = new ArrayList<>();
        lyricAdapter = new LyricAdapter(
                getContext(),
                lyrics,
                this,
                -1

        );

        binding.lyricRecycler.setAdapter(lyricAdapter);

        loadLyricsFromJson();

        return binding.getRoot();
    }


    @SuppressLint("NotifyDataSetChanged")
    public void updateCurrentSentence(String value) {
        lyricAdapter.notifyDataSetChanged();
        for (int i = 0; i < lyrics.size(); i++) {
            if (lyrics.get(i).getTime().equals(value)) {
                lyricAdapter.setCurrentSentence(i);
                smoothScrollToPositionCentered(i);
            }
        }
    }

    private void smoothScrollToPositionCentered(int position) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.lyricRecycler.getLayoutManager();
        int offsetInDp = -70;
        float density = requireContext().getResources().getDisplayMetrics().density;
        int offsetInPx = (int) (offsetInDp * density);
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(requireContext()) {
            @Override
            protected int getVerticalSnapPreference() {
                return SNAP_TO_START;
            }

            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                assert layoutManager != null;
                return layoutManager.computeScrollVectorForPosition(targetPosition);
            }

            @Override
            public float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                // Giảm giá trị này để cuộn chậm hơn → thời gian cuộn dài hơn
                return 20f / displayMetrics.densityDpi;
            }

            public AccelerateDecelerateInterpolator getInterpolator() {
                return new AccelerateDecelerateInterpolator();
            }

            @Override
            public int calculateDyToMakeVisible(View view, int snapPreference) {
                int dy = super.calculateDyToMakeVisible(view, snapPreference);
                return dy - offsetInPx;
            }


        };

        smoothScroller.setTargetPosition(position);
        assert layoutManager != null;
        layoutManager.startSmoothScroll(smoothScroller);
    }


    private boolean checkKeyExist(String key) {
        for(LyricLine lyricLine : lyrics) {
            if(lyricLine.getTime().equals(key)) {
                return true;
            }
        }
        return false;
    }


    private void loadLyricsFromJson() {
        PlayActivity playActivity = getPlayActivity();
        if (playActivity == null || playActivity.getSong() == null) return;

        if (isPlayDownloadedSong) {
            loadLyricsFromLocal(playActivity);

        } else {
            loadLyricsFromServer(playActivity);
        }
    }

    private void loadLyricsFromServer(PlayActivity playActivity) {
        LoadFileApi api = ApiClient.getClient(requireContext()).create(LoadFileApi.class);
        try {
            api.getLyric(Constants.FILE_LOAD_ENDPOINT + playActivity.getSong().getLyrics())
                    .enqueue(new Callback<>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                            lyrics.clear();
                            if (response.body() == null) return;
                            for (Map.Entry<String, String> entry : response.body().entrySet()) {
                                lyrics.add(LyricLine.builder()
                                        .time(entry.getKey())
                                        .text(entry.getValue())
                                        .build());
                            }
                            lyricAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Call<Map<String, String>> call, Throwable t) {
                            Log.e("LyricFragment", "API failed: " + t.getMessage());
                        }
                    });
        } catch (Exception exp) {
            System.err.print(exp.getMessage());
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadLyricsFromLocal(PlayActivity playActivity) {
        try {
            Log.d("LyricFragment", String.valueOf(playActivity.getSong().getLyrics()));
            File file = new File(playActivity.getSong().getLyrics());
            if (!file.exists()) return;

            StringBuilder jsonBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            reader.close();

            JSONObject jsonObject = new JSONObject(jsonBuilder.toString());
            lyrics.clear();
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String time = keys.next();
                String text = jsonObject.getString(time);
                lyrics.add(LyricLine.builder().time(time).text(text).build());
            }
            lyricAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("LyricFragment", "Error reading local lyric file", e);
        }
    }

    private PlayActivity getPlayActivity() {
        return (PlayActivity) getActivity();
    }

    @Override
    public void onClick(String time) {
        int millis = convertTimeToMillis(time);
        PlayActivity playActivity = getPlayActivity();
        if (playActivity != null && playActivity.getMediaPlayer() != null) {
            playActivity.getMediaPlayer().seekTo(millis);
            playActivity.getBinding().positionText.setText(playActivity.getPositionText(millis));
            playActivity.getBinding().seekBar.setProgress(millis);
        }
    }

    private int convertTimeToMillis(String time) {
        try {
            String[] parts = time.split(":");
            int minutes = Integer.parseInt(parts[0]);
            int seconds = Integer.parseInt(parts[1]);
            return (minutes * 60 + seconds) * 1000;
        } catch (Exception e) {
            return 0;
        }
    }
}