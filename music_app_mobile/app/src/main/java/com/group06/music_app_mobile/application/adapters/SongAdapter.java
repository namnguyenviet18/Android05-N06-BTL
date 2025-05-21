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

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songList;
    private Context context;
    private int selectedPosition = -1;

    public SongAdapter(Context context, List<Song> songList) {
        this.context = context;
        this.songList = songList;
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

        // Xử lý trạng thái bài hát (đang phát hay không)
        if (position == selectedPosition) {
            // Khi item được chọn: Đổi màu nền của ConstraintLayout
            holder.itemLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_background_selected));
            holder.playButtonContainer.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            holder.playButtonContainer.setBackground(ContextCompat.getDrawable(context, R.drawable.gradient_background));
        } else {
            // Khi item không được chọn: Quay lại nền mặc định
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
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    // Cập nhật danh sách bài hát
    public void updateSongs(List<Song> newSongs) {
        this.songList = newSongs != null ? newSongs : new ArrayList<>();
        notifyDataSetChanged();
    }
    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvSongTitle, tvArtist;
        CardView playButtonContainer;
        ImageView ivPlay;
        ConstraintLayout itemLayout; // Thêm tham chiếu đến ConstraintLayout

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSongTitle = itemView.findViewById(R.id.tvSongTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            playButtonContainer = itemView.findViewById(R.id.playButtonContainer);
            ivPlay = itemView.findViewById(R.id.ivPlay);
            itemLayout = itemView.findViewById(R.id.item_song); // ID của ConstraintLayout, cần thêm trong XML
        }
    }
}