package com.group06.music_app_mobile.app_utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.group06.music_app_mobile.api_client.api.SongApi;
import com.group06.music_app_mobile.api_client.responses.SongResponse;
import com.group06.music_app_mobile.models.Song;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DownloadUtil {
    private static final String TAG = "DownloadUtil";
    private SQLiteHelper dbHelper;
    private SongApi songApi;
    public DownloadUtil(Context context) {
        this.dbHelper = new SQLiteHelper(context);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.SERVER_DOMAIN) // Đảm bảo base URL kết thúc bằng "/"
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.songApi = retrofit.create(SongApi.class);
    }

    // Phương thức mới: Tải bài hát dựa trên songId
    public boolean downloadSongById(Context context, long songId) {
        try {
            // Kiểm tra xem bài hát đã được tải chưa
            if (dbHelper.isSongDownloaded(songId)) {
                Log.d(TAG, "Bài hát với ID " + songId + " đã được tải.");
                return true;
            }

            // Gọi API để lấy chi tiết bài hát
            Call<Song> call = songApi.getSongById(songId);
            Response<Song> response = call.execute();

            if (!response.isSuccessful() || response.body() == null) {
                Log.e(TAG, "Không thể lấy chi tiết bài hát với ID: " + songId + " - Mã lỗi: " + response.code());
                return false;
            }

            Song song = response.body();

            // Kiểm tra các URL
            String audioUrl = song.getAudioUrl();
            String coverImageUrl = song.getCoverImageUrl();
            String lyricsUrl = song.getLyrics();

            if (audioUrl == null || coverImageUrl == null || lyricsUrl == null) {
                Log.e(TAG, "Một trong các URL bị null: audio=" + audioUrl + ", cover=" + coverImageUrl + ", lyrics=" + lyricsUrl);
                return false;
            }

            // Tạo thư mục lưu trữ
            File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "downloaded_songs");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Tải file audio
            String audioFileName = "song_" + songId + ".mp3";
            File audioFile = new File(directory, audioFileName);
            downloadFile(audioUrl, audioFile);

            // Tải file ảnh bìa
            String coverFileName = "cover_" + songId + ".jpg";
            File coverFile = new File(directory, coverFileName);
            downloadFile(coverImageUrl, coverFile);

            // Tải file lyrics
            String lyricsFileName = "lyrics_" + songId + ".json";
            File lyricsFile = new File(directory, lyricsFileName);
            downloadFile(lyricsUrl, lyricsFile);

            // Lưu đường dẫn cục bộ
            String localAudioPath = audioFile.getAbsolutePath();
            String localCoverImagePath = coverFile.getAbsolutePath();
            String localLyricsPath = lyricsFile.getAbsolutePath();

            // Lưu thông tin vào SQLite
            dbHelper.insertDownloadedSong(songId, song.getName(), song.getAuthorName(), song.getSingerName(),
                    localAudioPath, localCoverImagePath, localLyricsPath);
            Log.d(TAG, "Đã tải và lưu bài hát vào cơ sở dữ liệu: " + song.getName());
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tải bài hát với ID " + songId + ": " + e.getMessage());
            return false;
        }
    }

    private void downloadFile(String fileUrl, File outputFile) throws Exception {
        Call<ResponseBody> call = songApi.loadFile(fileUrl);
        Response<ResponseBody> response = call.execute();

        if (response.isSuccessful() && response.body() != null) {
            InputStream inputStream = response.body().byteStream();
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
        } else {
            throw new Exception("Không thể tải file từ URL: " + fileUrl + " - Mã lỗi: " + response.code());
        }
    }
}