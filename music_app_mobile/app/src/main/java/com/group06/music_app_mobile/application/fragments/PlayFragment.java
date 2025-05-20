package com.group06.music_app_mobile.application.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.group06.music_app_mobile.app_utils.ServerDestination;
import com.group06.music_app_mobile.app_utils.StorageService;
import com.group06.music_app_mobile.databinding.FragmentPlayBinding;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlayFragment extends Fragment {

    private FragmentPlayBinding binding;
    private Uri audioFileUri;
    private Uri coverImageUri;
    private Uri lyricFileUri;

    private static final int PERMISSION_REQUEST_CODE = 100;

    private final ActivityResultLauncher<Intent> audioPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    audioFileUri = result.getData().getData();
                    String fileName = getFileName(audioFileUri);
                    binding.editTextAudioFile.setText(fileName);
                }
            }
    );

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    coverImageUri = result.getData().getData();
                    String fileName = getFileName(coverImageUri);
                    binding.editTextCoverImage.setText(fileName);
                }
            }
    );

    private final ActivityResultLauncher<Intent> lyricPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    lyricFileUri = result.getData().getData();
                    String fileName = getFileName(lyricFileUri);
                    binding.editTextLyric.setText(fileName);
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPlayBinding.inflate(inflater, container, false);

        checkPermissions();
        setupClickListeners();

        return binding.getRoot();
    }

    private void setupClickListeners() {
        binding.editTextAudioFile.setOnClickListener(v -> selectAudioFile());
        binding.editTextCoverImage.setOnClickListener(v -> selectCoverImage());
        binding.editTextLyric.setOnClickListener(v -> selectLyricFile());
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        binding.buttonAddMusic.setOnClickListener(v -> addMusic());
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Quyền được cấp", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Quyền bị từ chối", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void selectAudioFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/mp3");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String[] mimeTypes = {"audio/mpeg", "audio/mp3"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        audioPickerLauncher.launch(Intent.createChooser(intent, "Chọn file MP3"));
    }

    private void selectCoverImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String[] mimeTypes = {"image/png", "image/jpeg", "image/jpg"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Chọn ảnh (PNG/JPEG)"));
    }

    private void selectLyricFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String[] mimeTypes = {"application/json", "text/json"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        lyricPickerLauncher.launch(Intent.createChooser(intent, "Chọn file JSON"));
    }

    private String getFileName(Uri uri) {
        String fileName = "";
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver()
                    .query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        fileName = cursor.getString(index);
                    }
                }
            }
        }
        if (fileName.isEmpty()) {
            fileName = uri.getPath();
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }
        return fileName;
    }

    private void addMusic() {
        String songName = binding.editTextSongName.getText().toString().trim();
        String authorName = binding.editTextAuthorName.getText().toString().trim();
        String singerName = binding.editTextSingerName.getText().toString().trim();
        boolean isPublic = binding.checkBoxPublic.isChecked();

        if (songName.isEmpty()) {
            binding.editTextSongName.setError("Tên bài hát là bắt buộc");
            binding.editTextSongName.requestFocus();
            return;
        }
        if (authorName.isEmpty()) {
            binding.editTextAuthorName.setError("Tên tác giả là bắt buộc");
            binding.editTextAuthorName.requestFocus();
            return;
        }
        if (singerName.isEmpty()) {
            binding.editTextSingerName.setError("Tên ca sĩ là bắt buộc");
            binding.editTextSingerName.requestFocus();
            return;
        }
        if (audioFileUri == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn file MP3", Toast.LENGTH_SHORT).show();
            return;
        }
        if (coverImageUri == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn ảnh bìa", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadMusicToServer(songName, authorName, singerName, isPublic);
    }

    private void uploadMusicToServer(String songName, String authorName, String singerName, boolean isPublic) {
        String authToken = StorageService.getInstance(requireContext()).getAccessToken();
        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để tiếp tục", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .protocols(Arrays.asList(Protocol.HTTP_1_1, Protocol.HTTP_2)) // Ép dùng HTTP/1.1 hoặc HTTP/2
                .build();

        try {
            RequestBody audioBody = createRequestBody(audioFileUri, "audio/mpeg");
            RequestBody coverImageBody = createRequestBody(coverImageUri, "image/jpeg");
            RequestBody lyricBody =  createRequestBody(lyricFileUri, "application/json") ;

            MultipartBody.Builder builder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("songName", songName)
                    .addFormDataPart("authorName", authorName)
                    .addFormDataPart("singerName", singerName)
                    .addFormDataPart("isPublic", String.valueOf(isPublic))
                    .addFormDataPart("audioFile", getFileName(audioFileUri), audioBody)
                    .addFormDataPart("coverImage", getFileName(coverImageUri), coverImageBody);

            if (lyricBody != null) {
                builder.addFormDataPart("lyricFile", getFileName(lyricFileUri), lyricBody);
            } else {
                MediaType jsonMediaType = MediaType.get("application/json; charset=utf-8");
                builder.addFormDataPart("lyricFile", "empty.json",
                        RequestBody.create(jsonMediaType, "{}"));
            }

            RequestBody requestBody = builder.build();

            Request request = new Request.Builder()
                    .url("http://" + ServerDestination.SERVER_HOST + ":" + ServerDestination.SERVER_PORT + "/api/v1/song/add")
                    .addHeader("Authorization", "Bearer " + authToken)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Lỗi khi tải lên: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    requireActivity().runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Thêm bài hát thành công!", Toast.LENGTH_SHORT).show();
                            clearForm();
                        } else {
                            String errorBody = response.body() != null ? response.body().toString() : "Lỗi không xác định";
                            Toast.makeText(requireContext(), "Lỗi: " + errorBody + " (Code: " + response.code() + ")", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
        }
    }

    private RequestBody createRequestBody(Uri uri, String mediaType) throws IOException {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("Không thể mở file");
        }
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.get(mediaType);
            }

            @Override
            public void writeTo(@NonNull okio.BufferedSink sink) throws IOException {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    sink.write(buffer, 0, bytesRead);
                }
                inputStream.close();
            }
        };
    }

    private void clearForm() {
        binding.editTextSongName.setText("");
        binding.editTextAuthorName.setText("");
        binding.editTextSingerName.setText("");
        binding.editTextAudioFile.setText("");
        binding.editTextCoverImage.setText("");
        binding.editTextLyric.setText("");
        binding.checkBoxPublic.setChecked(false);

        audioFileUri = null;
        coverImageUri = null;
        lyricFileUri = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}