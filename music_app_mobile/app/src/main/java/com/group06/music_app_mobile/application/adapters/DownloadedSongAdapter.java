package com.group06.music_app_mobile.application.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.app_utils.SQLiteHelper;
import com.group06.music_app_mobile.app_utils.enums.DataTransferBetweenScreens;
import com.group06.music_app_mobile.application.activities.PlayActivity;
import com.group06.music_app_mobile.models.Song;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DownloadedSongAdapter extends RecyclerView.Adapter<DownloadedSongAdapter.SongViewHolder> {

    private List<Song> songList;
    private Context context;
    private SQLiteHelper dbHelper;

    public DownloadedSongAdapter(Context context, List<Song> songList) {
        this.context = context;
        this.songList = songList != null ? songList : new ArrayList<>();
        this.dbHelper = new SQLiteHelper(context);
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_downloaded_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);

        // Hiển thị thông tin bài hát
        holder.tvSongTitle.setText(song.getName());
        holder.tvArtist.setText(song.getAuthorName() + (song.getSingerName() != null && !song.getSingerName().isEmpty() ? ", " + song.getSingerName() : ""));

        // Tải ảnh bìa từ file đã tải xuống
        String coverImagePath = song.getCoverImageUrl();
        if (coverImagePath != null && !coverImagePath.isEmpty()) {
            Glide.with(context)
                    .load(coverImagePath)
                    .placeholder(R.drawable.adipene)
                    .error(R.drawable.adipene)
                    .into(holder.ivSongCover);
        } else {
            holder.ivSongCover.setImageResource(R.drawable.adipene);
        }

        // Xử lý nút 3 chấm (btnMore)
        holder.btnMore.setOnClickListener(v -> {
            // Tạo menu popup
            PopupMenu popupMenu = new PopupMenu(context, holder.btnMore);
            popupMenu.getMenu().add(0, 1, 0, "Play");
            popupMenu.getMenu().add(0, 2, 0, "Delete");

            // Xử lý sự kiện khi chọn menu item
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == 1) {
                        // Play bài hát
                        toPlayActivity(position);
                        return true;
                    } else if (itemId == 2) {
                        // Xóa bài hát
                        deleteSong(position);
                        return true;
                    }
                    return false;
                }
            });

            // Hiển thị menu popup
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public void updateSongs(List<Song> newSongs) {
        this.songList = newSongs != null ? newSongs : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Chuyển đến PlayActivity để phát nhạc
    private void toPlayActivity(int currentSongPosition) {
        Intent intent = new Intent(context, PlayActivity.class);
        intent.putExtra(DataTransferBetweenScreens .SONG_LIST.name(), (Serializable) songList);
        intent.putExtra(DataTransferBetweenScreens.CURRENT_SONG_POSITION.name(), currentSongPosition);
        context.startActivity(intent);
    }

    // Xóa bài hát khỏi SQLite và danh sách hiển thị
    private void deleteSong(int position) {
        Song song = songList.get(position);

        // Xóa file cục bộ (audio, cover, lyrics)
        deleteLocalFiles(song);

        // Xóa khỏi SQLite
        dbHelper.deleteDownloadedSong(song.getId());

        // Xóa khỏi danh sách hiển thị
        songList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, songList.size());

        Toast.makeText(context, "Đã xóa bài hát: " + song.getName(), Toast.LENGTH_SHORT).show();
    }

    // Xóa các file cục bộ của bài hát
    private void deleteLocalFiles(Song song) {
        String audioPath = song.getFileUrl();
        String coverPath = song.getCoverImageUrl();
        String lyricsPath = song.getLyricsUrl();

        if (audioPath != null) {
            File audioFile = new File(audioPath);
            if (audioFile.exists()) audioFile.delete();
        }
        if (coverPath != null) {
            File coverFile = new File(coverPath);
            if (coverFile.exists()) coverFile.delete();
        }
        if (lyricsPath != null) {
            File lyricsFile = new File(lyricsPath);
            if (lyricsFile.exists()) lyricsFile.delete();
        }
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView ivSongCover;
        TextView tvSongTitle, tvArtist;
        ImageButton btnMore;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSongCover = itemView.findViewById(R.id.ivSongCover);
            tvSongTitle = itemView.findViewById(R.id.tvSongTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}