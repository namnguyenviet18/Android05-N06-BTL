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

import com.group06.music_app_mobile.databinding.FragmentPlayBinding;

public class PlayFragment extends Fragment {

    private FragmentPlayBinding binding;
    private Uri audioFileUri;
    private Uri coverImageUri;
    private Uri lyricFileUri;

    private static final int PERMISSION_REQUEST_CODE = 100;

    // ActivityResultLauncher để chọn file audio
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

    // ActivityResultLauncher để chọn file hình ảnh
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

    // ActivityResultLauncher để chọn file lyric
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
        // Xử lý click trực tiếp vào EditText cho file audio
        binding.editTextAudioFile.setOnClickListener(v -> selectAudioFile());

        // Xử lý click trực tiếp vào EditText cho cover image
        binding.editTextCoverImage.setOnClickListener(v -> selectCoverImage());

        // Xử lý click trực tiếp vào EditText cho lyric file
        binding.editTextLyric.setOnClickListener(v -> selectLyricFile());

        // Xử lý nút back trên toolbar
        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // Xử lý nút Add Music
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
                Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void selectAudioFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/mp3");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Thêm các MIME types cho file audio
        String[] mimeTypes = {"audio/mpeg", "audio/mp3"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        audioPickerLauncher.launch(Intent.createChooser(intent, "Select MP3 File"));
    }

    private void selectCoverImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Giới hạn chỉ PNG và JPEG
        String[] mimeTypes = {"image/png", "image/jpeg", "image/jpg"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Image (PNG/JPEG)"));
    }

    private void selectLyricFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Chỉ cho phép file JSON
        String[] mimeTypes = {"application/json", "text/json"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        lyricPickerLauncher.launch(Intent.createChooser(intent, "Select JSON File"));
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
        // Lấy dữ liệu từ các EditText
        String songName = binding.editTextSongName.getText().toString().trim();
        String authorName = binding.editTextAuthorName.getText().toString().trim();
        String singerName = binding.editTextSingerName.getText().toString().trim();
        boolean isPublic = binding.checkBoxPublic.isChecked();

        // Validate dữ liệu
        if (songName.isEmpty()) {
            binding.editTextSongName.setError("Song name is required");
            binding.editTextSongName.requestFocus();
            return;
        }
        if (authorName.isEmpty()) {
            binding.editTextAuthorName.setError("Author name is required");
            binding.editTextAuthorName.requestFocus();
            return;
        }
        if (singerName.isEmpty()) {
            binding.editTextSingerName.setError("Singer name is required");
            binding.editTextSingerName.requestFocus();
            return;
        }
        if (audioFileUri == null) {
            Toast.makeText(requireContext(), "Please select an MP3 file", Toast.LENGTH_SHORT).show();
            return;
        }
        if (coverImageUri == null) {
            Toast.makeText(requireContext(), "Please select a cover image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload files to server
        uploadMusicToServer(songName, authorName, singerName, isPublic);
    }

    private void uploadMusicToServer(String songName, String authorName, String singerName, boolean isPublic) {
        // TODO: Implement upload logic
        Toast.makeText(requireContext(), "Uploading...", Toast.LENGTH_SHORT).show();

        // Giả lập upload thành công
        Toast.makeText(requireContext(), "Music added successfully!", Toast.LENGTH_SHORT).show();

        // Reset form
        clearForm();
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