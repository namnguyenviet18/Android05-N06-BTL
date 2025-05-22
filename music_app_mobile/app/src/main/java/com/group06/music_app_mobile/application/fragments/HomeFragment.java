package com.group06.music_app_mobile.application.fragments;

import static com.group06.music_app_mobile.app_utils.ServerDestination.SERVER_HOST;
import static com.group06.music_app_mobile.app_utils.ServerDestination.SERVER_PORT;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.PlaylistApi;
import com.group06.music_app_mobile.api_client.api.SongApi;
import com.group06.music_app_mobile.api_client.api.UserApi;
import com.group06.music_app_mobile.api_client.responses.PlaylistResponse;
import com.group06.music_app_mobile.app_utils.DownloadUtil;
import com.group06.music_app_mobile.app_utils.StorageService;
import com.group06.music_app_mobile.app_utils.enums.DataTransferBetweenScreens;
import com.group06.music_app_mobile.application.activities.PlayActivity;
import com.group06.music_app_mobile.application.adapters.HomeAdapter;
import com.group06.music_app_mobile.application.adapters.HotPlaylistAdapter;
import com.group06.music_app_mobile.application.adapters.PlaylistDialogAdapter;
import com.group06.music_app_mobile.application.events.OnLongItemClickListener;
import com.group06.music_app_mobile.application.events.OnSongItemClickListener;
import com.group06.music_app_mobile.models.Playlist;
import com.group06.music_app_mobile.models.Song;
import com.group06.music_app_mobile.models.User;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements OnSongItemClickListener, OnLongItemClickListener {
    private static final String TAG = "HomeFragment"; // Tag cho Log
    private static final int REQUEST_CODE_IMAGE_PERMISSION = 100;
    private RecyclerView recyclerRecent;
    private HomeAdapter adapter;
    private RecyclerView partyRecyclerView;
    private HotPlaylistAdapter hotPlaylistAdapter;
    private List<Playlist> playlistList;
    private List<Song> songList;
    private DownloadUtil downloadUtil; // Thêm DownloadUtil
    private PlaylistApi playlistApi;
    private SongApi songApi;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;
    private ImageView imagePreview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo RecycleView All song
        recyclerRecent = view.findViewById(R.id.recyclerRecent);

        // Khởi tạo RecycleView Hot Playlist
        partyRecyclerView = view.findViewById(R.id.partyRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        partyRecyclerView.setLayoutManager(layoutManager);
        partyRecyclerView.setHasFixedSize(true);
        partyRecyclerView.setClipToPadding(false);
        partyRecyclerView.setPadding(16, 0, 16, 0);

        // Khởi tạo adapter với danh sách rỗng để tránh lỗi "No adapter attached"
        playlistList = new ArrayList<>();
        hotPlaylistAdapter = new HotPlaylistAdapter(playlistList);
        partyRecyclerView.setAdapter(hotPlaylistAdapter);

        // Thêm PagerSnapHelper để hiển thị 2 CardView một lúc
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(partyRecyclerView);

        // Khởi tạo Adapter với danh sách rỗng cho recyclerRecent
        songList = new ArrayList<>();
        downloadUtil = new DownloadUtil(requireContext());
        adapter = new HomeAdapter(songList, this, this);
        recyclerRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerRecent.setAdapter(adapter);

        // Khởi tạo API
        playlistApi = ApiClient.getClient(getContext()).create(PlaylistApi.class);
        songApi = ApiClient.getClient(getContext()).create(SongApi.class);

        // Gọi API để lấy dữ liệu
        fetchPlaylists();
        fetchSongs();

        // Xử lý sự kiện click cho "See all"
        View seeAllTextView = view.findViewById(R.id.seeAllTextView);
        seeAllTextView.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AllSongsFragment())
                    .addToBackStack(null)
                    .commit();
            requireActivity().findViewById(R.id.viewPager).setVisibility(View.GONE);
            requireActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);
            requireActivity().findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        });

        View seeAllTextView2 = view.findViewById(R.id.seeAllTextView2);
        seeAllTextView2.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AllPlaylistsFragment())
                    .addToBackStack(null)
                    .commit();
            requireActivity().findViewById(R.id.viewPager).setVisibility(View.GONE);
            requireActivity().findViewById(R.id.bottomNavigationView).setVisibility(View.GONE);
            requireActivity().findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        });

        hotPlaylistAdapter.setOnItemClickListener(playlist -> {
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

        // Khởi tạo ActivityResultLauncher để chọn ảnh
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Log.d(TAG, "imagePickerLauncher result: " + result.getResultCode());
            if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
                Log.d(TAG, "Selected image URI: " + selectedImageUri);
                if (imagePreview != null && selectedImageUri != null) {
                    Glide.with(requireContext())
                            .load(selectedImageUri)
                            .error(R.drawable.ic_download_outline)
                            .into(imagePreview);
                    imagePreview.setVisibility(View.VISIBLE);
                    Log.d(TAG, "Image set to ImageView using Glide");
                } else {
                    Log.e(TAG, "Failed to set image: imagePreview=" + imagePreview + ", selectedImageUri=" + selectedImageUri);
                    Toast.makeText(requireContext(), "Không thể hiển thị ảnh", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Image picker failed: resultCode=" + result.getResultCode());
            }
        });

        return view;
    }


    private void fetchPlaylists() {
        Log.d(TAG, "Bắt đầu gọi API /playlist");
        Call<List<Playlist>> call = playlistApi.getLimitedPlaylists(4);

        call.enqueue(new Callback<List<Playlist>>() {
            @Override
            public void onResponse(Call<List<Playlist>> call, Response<List<Playlist>> response) {
                Log.d(TAG, "Nhận phản hồi từ API, mã trạng thái: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Dữ liệu nhận được, số playlist: " + response.body().size());
                    playlistList.clear();
                    playlistList.addAll(response.body());
                    hotPlaylistAdapter.notifyDataSetChanged();
                } else {
                    String errorMessage = switch (response.code()) {
                        case 400 -> "Yêu cầu không hợp lệ";
                        case 404 -> "Không tìm thấy playlist";
                        default -> "Lỗi khi lấy danh sách playlist: " + response.code();
                    };
                    Log.e(TAG, "Lỗi từ server: " + errorMessage);
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Playlist>> call, Throwable t) {
                Log.e(TAG, "Lỗi kết nối: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchSongs() {
        Log.d(TAG, "Bắt đầu gọi API song/list");
        Call<List<Song>> call = songApi.getAllSongs();

        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                Log.d(TAG, "Nhận phản hồi từ API, mã trạng thái: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Dữ liệu nhận được, số bài hát: " + response.body().size());
                    songList = response.body();
                    adapter.updateSongs(songList);
                } else {
                    String errorMessage = switch (response.code()) {
                        case 400 -> "Yêu cầu không hợp lệ";
                        case 404 -> "Không tìm thấy bài hát";
                        default -> "Lỗi khi lấy danh sách bài hát: " + response.code();
                    };
                    Log.e(TAG, "Lỗi từ server: " + errorMessage);
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Log.e(TAG, "Lỗi kết nối: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
            } else if (item.getItemId() == R.id.add_to_playlist_item) {
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
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "Lỗi khi tải playlist: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Playlist>> call, Throwable t) {
                Log.e(TAG, "Lỗi khi lấy playlist: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
            dialog.dismiss();
            showCreatePlaylistDialog();
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
                Log.e(TAG, "Lỗi khi thêm/xóa bài hát: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreatePlaylistDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_playlist, null);
        EditText playlistNameInput = dialogView.findViewById(R.id.playlist_name_input);
        Spinner visibilitySpinner = dialogView.findViewById(R.id.visibility_spinner);
        imagePreview = dialogView.findViewById(R.id.image_preview);
        Button selectImageButton = dialogView.findViewById(R.id.select_image_button);
        Button createPlaylistButton = dialogView.findViewById(R.id.create_playlist_button);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new String[]{"Public", "Private"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        visibilitySpinner.setAdapter(spinnerAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialog);
        builder.setView(dialogView);
        AlertDialog createDialog = builder.create();
        createDialog.setOnDismissListener(dialog -> imagePreview = null);
        createDialog.show();

        Window window = createDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            params.width = (int) (requireActivity().getResources().getDisplayMetrics().widthPixels * 0.8);
            window.setAttributes(params);
        }

        selectImageButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_IMAGE_PERMISSION);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            }
        });

        createPlaylistButton.setOnClickListener(v -> {
            String playlistName = playlistNameInput.getText().toString().trim();
            boolean isPublic = visibilitySpinner.getSelectedItem().toString().equals("Public");

            if (playlistName.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập tên playlist", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn ảnh bìa cho playlist", Toast.LENGTH_SHORT).show();
                return;
            }

            String authToken = StorageService.getInstance(requireContext()).getAccessToken();
            if (authToken == null || authToken.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
                return;
            }

            MultipartBody.Part coverImagePart = null;
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedImageUri);
                if (inputStream != null) {
                    File file = new File(requireContext().getCacheDir(), "cover_image.jpg");
                    java.nio.file.Files.copy(inputStream, file.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    inputStream.close();
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                    coverImagePart = MultipartBody.Part.createFormData("coverImage", file.getName(), requestFile);
                }
            } catch (IOException e) {
                Log.e(TAG, "Lỗi khi xử lý ảnh: " + e.getMessage());
                Toast.makeText(requireContext(), "Lỗi khi xử lý ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Playlist name sent: " + playlistName);
            Call<PlaylistResponse> call = playlistApi.createPlaylist(playlistName, isPublic, coverImagePart, "Bearer " + authToken);
            call.enqueue(new Callback<PlaylistResponse>() {
                @Override
                public void onResponse(Call<PlaylistResponse> call, Response<PlaylistResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(requireContext(), "Tạo playlist thành công: " + playlistName, Toast.LENGTH_SHORT).show();
                        playlistNameInput.setText("");
                        visibilitySpinner.setSelection(0);
                        selectedImageUri = null;
                        if (imagePreview != null) {
                            imagePreview.setImageDrawable(null);
                            imagePreview.setVisibility(View.GONE);
                        }
                        createDialog.dismiss();
                    } else {
                        Log.e(TAG, "Lỗi API: " + response.code());
                        Toast.makeText(requireContext(), "Lỗi khi tạo playlist: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PlaylistResponse> call, Throwable t) {
                    Log.e(TAG, "Lỗi kết nối: " + t.getMessage());
                    Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_IMAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            } else {
                Toast.makeText(requireContext(), "Quyền truy cập ảnh bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }
}