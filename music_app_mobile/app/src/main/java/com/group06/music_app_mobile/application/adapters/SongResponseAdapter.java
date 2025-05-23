package com.group06.music_app_mobile.application.adapters;

import static com.group06.music_app_mobile.app_utils.ServerDestination.SERVER_HOST;
import static com.group06.music_app_mobile.app_utils.ServerDestination.SERVER_PORT;

import android.annotation.SuppressLint;
import android.content.Context;
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

public class SongResponseAdapter extends RecyclerView.Adapter<SongResponseAdapter.SongViewHolder> {

    private List<SongResponse> songList;
    private final Context context;

    // MediaPlayer và Handler quản lý việc play nhạc và cập nhật UI thời gian
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler(Looper.getMainLooper());

    // Để lưu ViewHolder hiện tại đang phát để update UI thời gian
    private SongViewHolder currentPlayingHolder = null;

    // Biến để lưu id bài hát đang phát, -1 nếu không có bài nào đang phát
    private long playingSongId = -1;

    private OnSongItemClickListener onSongItemClickListener;
    private OnLongItemClickListener onLongItemClickListener;

    public SongResponseAdapter(Context context, List<SongResponse> songList) {
        this.context = context;
        this.songList = new ArrayList<>(songList != null ? songList : new ArrayList<>());
        mediaPlayer = new MediaPlayer();
    }

    public SongResponseAdapter(
            Context context,
            List<SongResponse> songList,
            OnSongItemClickListener onSongItemClickListener,
            OnLongItemClickListener onLongItemClickListener
    ) {
        this.context = context;
        this.songList = new ArrayList<>(songList != null ? songList : new ArrayList<>());
        mediaPlayer = new MediaPlayer();
        this.onSongItemClickListener = onSongItemClickListener;
        this.onLongItemClickListener = onLongItemClickListener;
    }

    public void setOnItemClickListener(OnSongItemClickListener listener) {
        this.onSongItemClickListener = listener;
    }

    public void setOnLongItemClickListener(OnLongItemClickListener listener) {
        this.onLongItemClickListener = listener;
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
        SongResponse songResponse = songList.get(position);

        // Tải ảnh bìa
        String coverImageUrl = songResponse.getCoverImageUrl();
        String fullImageUrl = "http://" + SERVER_HOST + ":" + SERVER_PORT + "/api/v1/song/file/load?fullUrl=" + coverImageUrl;
        Glide.with(context)
                .load(fullImageUrl)
                .placeholder(R.drawable.meha)
                .error(R.drawable.meha)
                .into(holder.imgCover);

        // Thiết lập tiêu đề
        holder.txtTitle.setText(songResponse.getName() != null ? songResponse.getName() : "Không rõ");

        // Thiết lập phụ đề
        String subtitle = (songResponse.getAuthorName() != null ? songResponse.getAuthorName() : "Không rõ") +
                (songResponse.getSingerName() != null && !songResponse.getSingerName().isEmpty()
                        ? " • " + songResponse.getSingerName()
                        : "");
        holder.txtSubtitle.setText(subtitle);

        // Thiết lập thời lượng
        long durationInMillis = songResponse.getDuration() * 1000; // Chuyển từ giây sang mili giây
        String formattedDuration = formatTime(durationInMillis);
        String displayTime = "0:00 / " + (durationInMillis > 0 ? formattedDuration : "?:??");
        if (songResponse.getId() == playingSongId && mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int totalDuration = mediaPlayer.getDuration();
            displayTime = formatTime(currentPosition) + " / " + formatTime(totalDuration);
        }
        holder.txtDuration.setText(displayTime);

        // Set icon play hoặc pause
        if (songResponse.getId() == playingSongId) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                holder.imgPlay.setImageResource(R.drawable.ic_pause_fill);
            } else {
                holder.imgPlay.setImageResource(R.drawable.ic_play_fill);
            }
        } else {
            holder.imgPlay.setImageResource(R.drawable.ic_play_fill);
        }

        // Handler để cập nhật thời gian chạy
        Handler localHandler = new Handler(Looper.getMainLooper());
        Runnable updateTimeRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying() && songResponse.getId() == playingSongId) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    int totalDuration = mediaPlayer.getDuration();
                    holder.txtDuration.setText(formatTime(currentPosition) + " / " + formatTime(totalDuration));
                    localHandler.postDelayed(this, 500); // Cập nhật mỗi 500ms
                }
            }
        };

        // Xử lý sự kiện click trên nút play
        holder.imgPlay.setOnClickListener(v -> {
            if (songResponse.getId() == playingSongId) {
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        localHandler.removeCallbacks(updateTimeRunnable); // Dừng cập nhật thời gian
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        int totalDuration = mediaPlayer.getDuration();
                        holder.txtDuration.setText(formatTime(currentPosition) + " / " + formatTime(totalDuration));
                        holder.imgPlay.setImageResource(R.drawable.ic_play_fill);
                    } else {
                        mediaPlayer.start();
                        holder.imgPlay.setImageResource(R.drawable.ic_pause_fill);
                        localHandler.postDelayed(updateTimeRunnable, 0); // Bắt đầu cập nhật thời gian
                    }
                    notifyDataSetChanged();
                }
            } else {
                // Dừng bài hát hiện tại (nếu có)
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                    if (currentPlayingHolder != null) {
                        long originalDuration = songList.get(currentPlayingHolder.getAdapterPosition()).getDuration() * 1000;
                        currentPlayingHolder.txtDuration.setText("0:00 / " + formatTime(originalDuration));
                        currentPlayingHolder.imgPlay.setImageResource(R.drawable.ic_play_fill);
                    }
                    localHandler.removeCallbacks(updateTimeRunnable); // Dừng cập nhật thời gian
                }

                // Chuyển đổi SongResponse sang Song
                Song song = convertToSong(songResponse);

                String audioPath = songResponse.getAudioUrl();
                String audioUrl = "http://" + SERVER_HOST + ":" + SERVER_PORT + "/api/v1/song/file/load?fullUrl=" + audioPath;

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(audioUrl);
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(mp -> {
                        mp.start();
                        playingSongId = songResponse.getId();
                        currentPlayingHolder = holder;
                        holder.imgPlay.setImageResource(R.drawable.ic_pause_fill);

                        int totalDuration = mp.getDuration();
                        if (durationInMillis <= 0) {
                            holder.txtDuration.setText("0:00 / " + formatTime(totalDuration));
                        }

                        localHandler.postDelayed(updateTimeRunnable, 0); // Bắt đầu cập nhật thời gian
                        notifyDataSetChanged();
                    });
                    mediaPlayer.setOnCompletionListener(mp -> {
                        playingSongId = -1;
                        localHandler.removeCallbacks(updateTimeRunnable); // Dừng cập nhật thời gian
                        holder.txtDuration.setText("0:00 / " + formatTime(durationInMillis > 0 ? durationInMillis : mp.getDuration()));
                        holder.imgPlay.setImageResource(R.drawable.ic_play_fill);
                        currentPlayingHolder = null;
                        notifyDataSetChanged();
                    });
                    mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                        localHandler.removeCallbacks(updateTimeRunnable); // Dừng cập nhật thời gian
                        Toast.makeText(context, "Lỗi phát nhạc", Toast.LENGTH_SHORT).show();
                        return true;
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    localHandler.removeCallbacks(updateTimeRunnable); // Dừng cập nhật thời gian
                    Toast.makeText(context, "Không thể phát nhạc", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Xử lý sự kiện click trên item
        holder.songItem.setOnClickListener(view -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            if (onSongItemClickListener != null) {
                onSongItemClickListener.toPlayActivity(position);
            }
        });

        // Xử lý sự kiện long click trên item
        holder.songItem.setOnLongClickListener(view -> {
            if (onLongItemClickListener != null) {
                onLongItemClickListener.onLongItemClick(convertToSong(songResponse), view);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public void updateSongs(List<SongResponse> newSongs) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SongDiffCallback(songList, newSongs));
        songList.clear();
        if (newSongs != null) {
            songList.addAll(newSongs);
        }
        diffResult.dispatchUpdatesTo(this);
    }

    private static class SongDiffCallback extends DiffUtil.Callback {
        private final List<SongResponse> oldList;
        private final List<SongResponse> newList;

        SongDiffCallback(List<SongResponse> oldList, List<SongResponse> newList) {
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
            SongResponse oldSong = oldList.get(oldItemPosition);
            SongResponse newSong = newList.get(newItemPosition);
            return oldSong.getName().equals(newSong.getName()) &&
                    oldSong.getAuthorName().equals(newSong.getAuthorName()) &&
                    (oldSong.getSingerName() == null ? newSong.getSingerName() == null : oldSong.getSingerName().equals(newSong.getSingerName()));
        }
    }

    // Phương thức chuyển đổi SongResponse sang Song
    private Song convertToSong(SongResponse songResponse) {
        Song song = new Song(); // Giả định Song có constructor mặc định
        song.setId(songResponse.getId());
        song.setName(songResponse.getName());
        song.setAuthorName(songResponse.getAuthorName());
        song.setSingerName(songResponse.getSingerName());
        song.setAudioUrl(songResponse.getAudioUrl());
        song.setCoverImageUrl(songResponse.getCoverImageUrl());
        song.setLyrics(songResponse.getLyrics());
        song.setPublic(songResponse.isPublic());
        song.setDuration(songResponse.getDuration() * 1000); // Chuyển từ giây sang mili giây
        return song;
    }
}