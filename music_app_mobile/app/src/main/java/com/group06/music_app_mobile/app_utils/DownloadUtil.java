package com.group06.music_app_mobile.app_utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.SongApi;
import com.group06.music_app_mobile.models.Song;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadUtil {
    private static final String TAG = "DownloadUtil";
    private SQLiteHelper dbHelper;

    public DownloadUtil(Context context) {
        this.dbHelper = new SQLiteHelper(context);
    }

    // Phương thức: Tải bài hát dựa trên songId
    public boolean downloadSongById(Context context, long songId) {
        // Kiểm tra xem bài hát đã được tải chưa
        if (dbHelper.isSongDownloaded(songId)) {
            Toast.makeText(context, "Bài hát này đã được tải!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Gọi API để lấy chi tiết bài hát bất đồng bộ
        SongApi songApi = ApiClient.getClient(context).create(SongApi.class);
        Call<Song> call = songApi.getSongById(songId);
        call.enqueue(new Callback<Song>() {
            @Override
            public void onResponse(Call<Song> call, Response<Song> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Không thể lấy chi tiết bài hát với ID: " + songId + " - Mã lỗi: " + response.code());
                    Toast.makeText(context, "Không thể lấy chi tiết bài hát!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Song song = response.body();

                // Kiểm tra các URL
                String audioUrl = song.getAudioUrl();
                String coverImageUrl = song.getCoverImageUrl();
                String lyricsUrl = song.getLyrics();

                if (audioUrl == null || coverImageUrl == null || lyricsUrl == null) {
                    Log.e(TAG, "Một trong các URL bị null: audio=" + audioUrl + ", cover=" + coverImageUrl + ", lyrics=" + lyricsUrl);
                    Toast.makeText(context, "Dữ liệu bài hát không đầy đủ!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Tạo thư mục lưu trữ
                File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "downloaded_songs");
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Tải file audio
                String audioFileName = "song_" + songId + ".mp3";
                File audioFile = new File(directory, audioFileName);
                downloadFile(audioUrl, audioFile, new DownloadCallback() {
                    @Override
                    public void onSuccess() {
                        // Tải file ảnh bìa
                        String coverFileName = "cover_" + songId + ".jpg";
                        File coverFile = new File(directory, coverFileName);
                        downloadFile(coverImageUrl, coverFile, new DownloadCallback() {
                            @Override
                            public void onSuccess() {
                                // Tải file lyrics
                                String lyricsFileName = "lyrics_" + songId + ".json";
                                File lyricsFile = new File(directory, lyricsFileName);
                                downloadFile(lyricsUrl, lyricsFile, new DownloadCallback() {
                                    @Override
                                    public void onSuccess() {
                                        // Lưu đường dẫn cục bộ
                                        String localAudioPath = audioFile.getAbsolutePath();
                                        String localCoverImagePath = coverFile.getAbsolutePath();
                                        String localLyricsPath = lyricsFile.getAbsolutePath();

                                        // Lưu thông tin vào SQLite
                                        dbHelper.insertDownloadedSong(songId, song.getName(), song.getAuthorName(), song.getSingerName(),
                                                localAudioPath, localCoverImagePath, localLyricsPath);
                                        Log.d(TAG, "Đã tải và lưu bài hát vào cơ sở dữ liệu: " + song.getName());
                                        Toast.makeText(context, "Tải bài hát thành công: " + song.getName(), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        Log.e(TAG, "Lỗi khi tải lyrics: " + error);
                                        Toast.makeText(context, "Lỗi khi tải lyrics!", Toast.LENGTH_SHORT).show();
                                    }
                                }, context);
                            }

                            @Override
                            public void onFailure(String error) {
                                Log.e(TAG, "Lỗi khi tải ảnh bìa: " + error);
                                Toast.makeText(context, "Lỗi khi tải ảnh bìa!", Toast.LENGTH_SHORT).show();
                            }
                        }, context);
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, "Lỗi khi tải audio: " + error);
                        Toast.makeText(context, "Lỗi khi tải audio!", Toast.LENGTH_SHORT).show();
                    }
                }, context);
            }

            @Override
            public void onFailure(Call<Song> call, Throwable t) {
                Log.e(TAG, "Lỗi khi gọi API lấy chi tiết bài hát với ID " + songId + ": " + t.getMessage());
                Toast.makeText(context, "Lỗi khi lấy chi tiết bài hát!", Toast.LENGTH_SHORT).show();
            }
        });

        // Vì enqueue là bất đồng bộ, không thể trả về boolean trực tiếp
        return true;
    }

    // Lấy danh sách bài hát đã tải xuống từ SQLite
    public List<Song> getDownloadedSongs() {
        return dbHelper.getAllDownloadedSongs();
    }

    // Callback interface để xử lý bất đồng bộ
    private interface DownloadCallback {
        void onSuccess();
        void onFailure(String error);
    }

    private void downloadFile(String fileUrl, File outputFile, DownloadCallback callback, Context context) {
        SongApi songApi = ApiClient.getClient(context).create(SongApi.class);
        Call<ResponseBody> call = songApi.loadFile(fileUrl);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        InputStream inputStream = response.body().byteStream();
                        FileOutputStream outputStream = new FileOutputStream(outputFile);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.close();
                        inputStream.close();
                        callback.onSuccess();
                    } catch (Exception e) {
                        callback.onFailure("Không thể lưu file: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("Không thể tải file từ URL: " + fileUrl + " - Mã lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure("Lỗi khi tải file: " + t.getMessage());
            }
        });
    }
}