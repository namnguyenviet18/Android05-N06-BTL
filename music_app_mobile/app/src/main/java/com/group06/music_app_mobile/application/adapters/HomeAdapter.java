package com.group06.music_app_mobile.application.adapters;

import static com.group06.music_app_mobile.app_utils.ServerDestination.SERVER_HOST;
import static com.group06.music_app_mobile.app_utils.ServerDestination.SERVER_PORT;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.responses.SongResponse;
import com.group06.music_app_mobile.application.events.OnLongItemClickListener;
import com.group06.music_app_mobile.application.events.OnSongItemClickListener;
import com.group06.music_app_mobile.models.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.SongViewHolder> {

    private List<Song> songs;

    // MediaPlayer và Handler quản lý việc play nhạc và cập nhật UI thời gian
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler(Looper.getMainLooper());

    // Để lưu ViewHolder hiện tại đang phát để update UI thời gian
    private SongViewHolder currentPlayingHolder = null;

    // Biến để lưu id bài hát đang phát, -1 nếu không có bài nào đang phát
    private long playingSongId = -1;

    private OnSongItemClickListener onSongItemClickListener;

    private OnLongItemClickListener onLongItemClickListener;

    public HomeAdapter(List<Song> songs, OnSongItemClickListener onSongItemClickListener) {
        this.songs = new ArrayList<>(songs);
        mediaPlayer = new MediaPlayer();
        this.onSongItemClickListener = onSongItemClickListener;
    }

    public HomeAdapter(
            List<Song> songs,
            OnSongItemClickListener onSongItemClickListener,
            OnLongItemClickListener onLongItemClickListener
    ) {
        this.songs = new ArrayList<>(songs);
        mediaPlayer = new MediaPlayer();
        this.onSongItemClickListener = onSongItemClickListener;
        this.onLongItemClickListener = onLongItemClickListener;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover, imgPlay;
        TextView txtTitle, txtSubtitle, txtDuration;

        LinearLayout songItem;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            imgPlay = itemView.findViewById(R.id.imgPlay);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtSubtitle = itemView.findViewById(R.id.txtSubtitle);
            txtDuration = itemView.findViewById(R.id.txtDuration);
            songItem = itemView.findViewById(R.id.song_item);
        }
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_song, parent, false);
        return new SongViewHolder(view);
    }

    private String formatTime(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);

        holder.txtTitle.setText(song.getName());
        holder.txtSubtitle.setText(song.getAuthorName() +
                (song.getSingerName() != null && !song.getSingerName().isEmpty()
                        ? " • " + song.getSingerName()
                        : ""));

        long durationInMillis = song.getDuration();
        String formattedDuration = formatTime(durationInMillis);

        // Hiển thị thời gian ban đầu hoặc thời gian hiện tại
        String displayTime = "0:00 / " + (durationInMillis > 0 ? formattedDuration : "?:??");
        if (song.getId() == playingSongId && mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int totalDuration = mediaPlayer.getDuration();
            displayTime = formatTime(currentPosition) + " / " + formatTime(totalDuration);
        }
        holder.txtDuration.setText(displayTime);

        String coverImageUrl = song.getCoverImageUrl();
        String fullImageUrl = "http://" + SERVER_HOST + ":" + SERVER_PORT + "/api/v1/song/file/load?fullUrl=" + coverImageUrl;

        Glide.with(holder.itemView.getContext())
                .load(fullImageUrl)
                .placeholder(R.drawable.meha)
                .error(R.drawable.meha)
                .into(holder.imgCover);

        // Set icon play hoặc pause
        if (song.getId() == playingSongId) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                holder.imgPlay.setImageResource(R.drawable.ic_pause_fill);
            } else {
                holder.imgPlay.setImageResource(R.drawable.ic_play_fill);
            }
        } else {
            holder.imgPlay.setImageResource(R.drawable.ic_play_fill);
        }

        // Handler để cập nhật thời gian chạy
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying() && song.getId() == playingSongId) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    int totalDuration = mediaPlayer.getDuration();
                    holder.txtDuration.setText(formatTime(currentPosition) + " / " + formatTime(totalDuration));
                    handler.postDelayed(this, 500); // Cập nhật mỗi 500ms
                }
            }
        };

        holder.imgPlay.setOnClickListener(v -> {
            if (song.getId() == playingSongId) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        handler.removeCallbacks(updateTimeRunnable); // Dừng cập nhật thời gian
                        // Hiển thị thời gian tại vị trí tạm dừng
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        int totalDuration = mediaPlayer.getDuration();
                        holder.txtDuration.setText(formatTime(currentPosition) + " / " + formatTime(totalDuration));
                        holder.imgPlay.setImageResource(R.drawable.ic_play_fill);
                    } else {
                        mediaPlayer.start();
                        holder.imgPlay.setImageResource(R.drawable.ic_pause_fill);
                        handler.postDelayed(updateTimeRunnable, 0); // Bắt đầu cập nhật thời gian
                    }
                    notifyDataSetChanged();
                }
            } else {
                // Dừng bài hát hiện tại (nếu có)
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    handler.removeCallbacks(updateTimeRunnable); // Dừng cập nhật thời gian
                }

                String audioPath = song.getAudioUrl();
                String audioUrl = "http://" + SERVER_HOST + ":" + SERVER_PORT + "/api/v1/song/file/load?fullUrl=" + audioPath;

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(audioUrl);
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(mp -> {
                        mp.start();
                        playingSongId = song.getId();
                        holder.imgPlay.setImageResource(R.drawable.ic_pause_fill);

                        // Cập nhật thời lượng nếu chưa có từ server
                        int totalDuration = mp.getDuration();
                        if (durationInMillis <= 0) {
                            holder.txtDuration.setText("0:00 / " + formatTime(totalDuration));
                        }

                        // Bắt đầu cập nhật thời gian chạy
                        handler.postDelayed(updateTimeRunnable, 0);

                        notifyDataSetChanged();
                    });
                    mediaPlayer.setOnCompletionListener(mp -> {
                        playingSongId = -1;
                        handler.removeCallbacks(updateTimeRunnable); // Dừng cập nhật thời gian
                        holder.txtDuration.setText("0:00 / " + formatTime(durationInMillis > 0 ? durationInMillis : mp.getDuration()));
                        holder.imgPlay.setImageResource(R.drawable.ic_play_fill);
                        notifyDataSetChanged();
                    });
                    mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                        handler.removeCallbacks(updateTimeRunnable); // Dừng cập nhật thời gian
                        Toast.makeText(holder.itemView.getContext(), "Lỗi phát nhạc", Toast.LENGTH_SHORT).show();
                        return true;
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.removeCallbacks(updateTimeRunnable); // Dừng cập nhật thời gian
                    Toast.makeText(holder.itemView.getContext(), "Không thể phát nhạc", Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.songItem.setOnClickListener(view -> {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            onSongItemClickListener.toPlayActivity(position);
        });

        holder.songItem.setOnLongClickListener(view -> {
            if(onLongItemClickListener != null) {
                onLongItemClickListener.onLongItemClick(song, view);
            }
            return true;
        });
    }
    // Hàm định dạng thời lượng ms -> chuỗi mm:ss
    private String formatDuration(int durationMs) {
        int totalSeconds = durationMs / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }


    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void updateSongs(List<Song> newSongs) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SongDiffCallback(songs, newSongs));
        songs.clear();
        songs.addAll(newSongs);
        diffResult.dispatchUpdatesTo(this);
    }

    private static class SongDiffCallback extends DiffUtil.Callback {
        private final List<Song> oldList;
        private final List<Song> newList;

        SongDiffCallback(List<Song> oldList, List<Song> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Song oldSong = oldList.get(oldItemPosition);
            Song newSong = newList.get(newItemPosition);
            return oldSong.getName().equals(newSong.getName()) &&
                    oldSong.getAuthorName().equals(newSong.getAuthorName()) &&
                    (oldSong.getSingerName() == null ? newSong.getSingerName() == null : oldSong.getSingerName().equals(newSong.getSingerName()));
        }
    }
}
