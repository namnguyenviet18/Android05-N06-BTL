package com.group06.music_app_mobile.app_utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.group06.music_app_mobile.models.Song;

import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "music_app.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_DOWNLOADED_SONGS = "downloaded_songs";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_DOWNLOADED_SONGS + " (" +
                "id INTEGER PRIMARY KEY," +
                "name TEXT NOT NULL," +
                "author_name TEXT NOT NULL," +
                "singer_name TEXT NOT NULL," +
                "local_audio_path TEXT," +
                "local_cover_image_path TEXT," +
                "local_lyrics_path TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOWNLOADED_SONGS);
        onCreate(db);
    }

    public void insertDownloadedSong(long id, String name, String authorName, String singerName,
                                     String localAudioPath, String localCoverImagePath, String localLyricsPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("name", name);
        values.put("author_name", authorName);
        values.put("singer_name", singerName);
        if (localAudioPath != null) {
            values.put("local_audio_path", localAudioPath);
        }
        if (localCoverImagePath != null) {
            values.put("local_cover_image_path", localCoverImagePath);
        }
        if (localLyricsPath != null) {
            values.put("local_lyrics_path", localLyricsPath);
        }
        db.insertWithOnConflict(TABLE_DOWNLOADED_SONGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public boolean isSongDownloaded(long songId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DOWNLOADED_SONGS, new String[]{"local_audio_path"}, "id = ?",
                new String[]{String.valueOf(songId)}, null, null, null);
        boolean isDownloaded = false;
        if (cursor.moveToFirst()) {
            isDownloaded = cursor.getString(cursor.getColumnIndexOrThrow("local_audio_path")) != null;
        }
        cursor.close();
        db.close();
        return isDownloaded;
    }

    public String getLocalAudioPath(long songId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DOWNLOADED_SONGS, new String[]{"local_audio_path"}, "id = ?",
                new String[]{String.valueOf(songId)}, null, null, null);
        String path = null;
        if (cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndexOrThrow("local_audio_path"));
        }
        cursor.close();
        db.close();
        return path;
    }

    public String getLocalCoverImagePath(long songId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DOWNLOADED_SONGS, new String[]{"local_cover_image_path"}, "id = ?",
                new String[]{String.valueOf(songId)}, null, null, null);
        String path = null;
        if (cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndexOrThrow("local_cover_image_path"));
        }
        cursor.close();
        db.close();
        return path;
    }

    public String getLocalLyricsPath(long songId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DOWNLOADED_SONGS, new String[]{"local_lyrics_path"}, "id = ?",
                new String[]{String.valueOf(songId)}, null, null, null);
        String path = null;
        if (cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndexOrThrow("local_lyrics_path"));
        }
        cursor.close();
        db.close();
        return path;
    }

    // Thêm phương thức để lấy danh sách tất cả bài hát đã tải
    public List<Song> getAllDownloadedSongs() {
        List<Song> downloadedSongs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DOWNLOADED_SONGS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Song song = new Song();
                song.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                song.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                song.setAuthorName(cursor.getString(cursor.getColumnIndexOrThrow("author_name")));
                song.setSingerName(cursor.getString(cursor.getColumnIndexOrThrow("singer_name")));
                song.setFileUrl(cursor.getString(cursor.getColumnIndexOrThrow("local_audio_path")));
                song.setCoverImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("local_cover_image_path")));
                song.setLyricsUrl(cursor.getString(cursor.getColumnIndexOrThrow("local_lyrics_path")));
                downloadedSongs.add(song);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return downloadedSongs;
    }

    // Thêm phương thức để xóa bài hát khỏi cơ sở dữ liệu
    public void deleteDownloadedSong(long songId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DOWNLOADED_SONGS, "id = ?", new String[]{String.valueOf(songId)});
        db.close();
    }
}