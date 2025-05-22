package com.group06.music_app_mobile.application.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.responses.PlaylistResponse;
import com.group06.music_app_mobile.app_utils.ServerDestination;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    private static final String TAG = "PlaylistAdapter";
    private  List<PlaylistResponse> playlistList;
    private final Context context;
    private OnItemClickListener onItemClickListener; // Add listener interface

    // Interface for item click handling
    public interface OnItemClickListener {
        void onItemClick(PlaylistResponse playlist);
    }

    public PlaylistAdapter(Context context, List<PlaylistResponse> playlistList) {
        this.context = context;
        this.playlistList = playlistList != null ? new ArrayList<>(playlistList) : new ArrayList<>(); // Tạo bản sao để tránh tham chiếu trực tiếp
    }

    // Method to set the click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_item, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        PlaylistResponse playlist = playlistList.get(position);

        // Set playlist name
        holder.tvPlaylistTitle.setText(playlist.getName() != null ? playlist.getName() : "Unknown");

        // Set song count
        holder.tvSongCount.setText(playlist.getSongCount() + " bài hát");

        // Load cover image with Glide
        String imageUrl = playlist.getCoverImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (!imageUrl.startsWith("http")) {
                // Assuming ServerDestination is available
                imageUrl = "http://" + ServerDestination.SERVER_HOST + ":" + ServerDestination.SERVER_PORT + imageUrl;
            }
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_google)
                    .error(R.drawable.ic_google)
                    .into(holder.ivPlaylistImage);
        } else {
            holder.ivPlaylistImage.setImageResource(R.drawable.ic_google);
        }

        // Set click listener for the item
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(playlist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlaylistImage;
        TextView tvPlaylistTitle;
        TextView tvSongCount;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlaylistImage = itemView.findViewById(R.id.ivPlaylistImage);
            tvPlaylistTitle = itemView.findViewById(R.id.tvPlaylistTitle);
            tvSongCount = itemView.findViewById(R.id.tvSongCount);
        }
    }

    public void updatePlaylists(List<PlaylistResponse> newPlaylists) {
        System.out.println("Playlist size: " + (newPlaylists != null ? newPlaylists.size() : 0));
        if (newPlaylists != null) {
            newPlaylists.forEach(p -> System.out.println("Playlist item: " + p));
        } else {
            System.out.println("newPlaylists is null");
        }
        playlistList.clear();
        System.out.println("After clear, size: " + playlistList.size()); // Kiểm tra sau khi clear
        if (newPlaylists != null) {
            playlistList.addAll(newPlaylists);
            System.out.println("After addAll, size: " + playlistList.size()); // Kiểm tra sau khi addAll
            playlistList.forEach(p -> System.out.println("After item: " + p));
        }
        System.out.println("Final size: " + playlistList.size());
        notifyDataSetChanged();
    }
}