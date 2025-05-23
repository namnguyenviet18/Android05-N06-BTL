package com.group06.music_app_mobile.application.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.app_utils.DownloadUtil;
import com.group06.music_app_mobile.app_utils.enums.DataTransferBetweenScreens;
import com.group06.music_app_mobile.application.activities.PlayActivity;
import com.group06.music_app_mobile.application.adapters.DownloadedSongAdapter;
import com.group06.music_app_mobile.application.events.OnSongItemClickListener;
import com.group06.music_app_mobile.models.Song;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DownloadFragment extends Fragment implements OnSongItemClickListener {

    private RecyclerView recyclerViewDownloads;
    private DownloadedSongAdapter songAdapter; // Sử dụng DownloadedSongAdapter
    private List<Song> downloadedSongs;
    private DownloadUtil downloadUtil;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download, container, false);

        // Khởi tạo DownloadUtil
        downloadUtil = new DownloadUtil(getContext());

        // Khởi tạo RecyclerView
        recyclerViewDownloads = view.findViewById(R.id.recyclerViewDownloads);
        recyclerViewDownloads.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo danh sách và adapter
        downloadedSongs = new ArrayList<>();
        songAdapter = new DownloadedSongAdapter(getContext(), downloadedSongs, this); // Sử dụng DownloadedSongAdapter
        recyclerViewDownloads.setAdapter(songAdapter);

        // Lấy danh sách bài hát đã tải xuống
        fetchDownloadedSongs();

        return view;
    }

    private void fetchDownloadedSongs() {
        downloadedSongs.clear();
        List<Song> songs = downloadUtil.getDownloadedSongs();
        if (songs != null && !songs.isEmpty()) {
            downloadedSongs.clear();
            downloadedSongs.addAll(songs);
            songAdapter.updateSongs(downloadedSongs);
        } else {
            Toast.makeText(getContext(), "Bạn chưa tải bài hát nào!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchDownloadedSongs();
    }

    @Override
    public void toPlayActivity(int currentSongPosition) {
        Intent intent = new Intent(requireActivity(), PlayActivity.class);
        intent.putExtra(DataTransferBetweenScreens.SONG_LIST.name(), (Serializable) downloadedSongs);
        intent.putExtra(DataTransferBetweenScreens.CURRENT_SONG_POSITION.name(), currentSongPosition);
        intent.putExtra(DataTransferBetweenScreens.PLAY_DOWNLOADED_SONG.name(), true);
        startActivity(intent);
    }
}