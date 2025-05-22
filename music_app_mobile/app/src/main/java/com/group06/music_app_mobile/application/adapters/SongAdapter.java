package com.group06.music_app_mobile.application.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.models.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer; // Thêm để xử lý vị trí

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songList;
    private Context context;
    private int selectedPosition = -1;
    private Consumer<Song> onSongClickListener; // Callback để cập nhật ảnh bìa
    private Runnable onDownloadClickListener; // Callback để tải khi nhấn Download
    private IntConsumer onPlayClickListener; // Callback để chuyển sang PlayActivity

    public SongAdapter(Context context, List<Song> songList, Consumer<Song> onSongClickListener,
                       Runnable onDownloadClickListener, IntConsumer onPlayClickListener) {
        this.context = context;
        this.songList = songList != null ? songList : new ArrayList<>();
        this.onSongClickListener = onSongClickListener;
        this.onDownloadClickListener = onDownloadClickListener;
        this.onPlayClickListener = onPlayClickListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.tvSongTitle.setText(song.getName());
        holder.tvArtist.setText(song.getAuthorName() + (song.getSingerName() != null && !song.getSingerName().isEmpty() ? ", " + song.getSingerName() : ""));

        if (position == selectedPosition) {
            holder.itemLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_background_selected));
            holder.playButtonContainer.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            holder.playButtonContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient_background));
        } else {
            holder.itemLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_background));
            holder.playButtonContainer.setBackground(null);
            holder.playButtonContainer.setCardBackgroundColor(ContextCompat.getColor(context, R.color.bg_gray));
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            if (selectedPosition != RecyclerView.NO_POSITION) {
                if (previousPosition != -1) {
                    notifyItemChanged(previousPosition);
                }
                notifyItemChanged(selectedPosition);

                if (onSongClickListener != null) {
                    onSongClickListener.accept(song);
                }
            }
        });

        // Xử lý nhấn nút play
        holder.playButtonContainer.setOnClickListener(v -> {
            if (onPlayClickListener != null && position != RecyclerView.NO_POSITION) {
                onPlayClickListener.accept(position); // Truyền vị trí bài hát
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public void updateSongs(List<Song> newSongs) {
        this.songList = newSongs != null ? newSongs : new ArrayList<>();
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvSongTitle, tvArtist;
        CardView playButtonContainer;
        ImageView ivPlay;
        ConstraintLayout itemLayout;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSongTitle = itemView.findViewById(R.id.tvSongTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            playButtonContainer = itemView.findViewById(R.id.playButtonContainer);
            ivPlay = itemView.findViewById(R.id.ivPlay);
            itemLayout = itemView.findViewById(R.id.item_song);
        }
    }
}