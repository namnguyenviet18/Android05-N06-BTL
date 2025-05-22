package com.group06.music_app_mobile.application.adapters;

import static com.group06.music_app_mobile.app_utils.ServerDestination.SERVER_HOST;
import static com.group06.music_app_mobile.app_utils.ServerDestination.SERVER_PORT;

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
import com.group06.music_app_mobile.models.Playlist;

import java.util.List;

public class HotPlaylistAdapter extends RecyclerView.Adapter<HotPlaylistAdapter.PlaylistViewHolder> {

    private List<Playlist> playlists;
    private OnItemClickListener onItemClickListener;

    // Interface for item click handling
    public interface OnItemClickListener {
        void onItemClick(Playlist playlist);
    }

    public HotPlaylistAdapter(List<Playlist> playlists) {
        this.playlists = playlists;
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hot_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        holder.titleTextView.setText(playlist.getName());
        String coverImageUrl = playlist.getCoverImageUrl();
        String fullImageUrl = "http://" + SERVER_HOST + ":" + SERVER_PORT + "/api/v1/song/file/load?fullUrl=" + coverImageUrl;

        // Load image with Glide
        Glide.with(holder.itemView.getContext())
                .load(fullImageUrl)
                .placeholder(R.drawable.friday_party) // Use your placeholder drawable
                .into(holder.imageView);

        // Set click listener for the item
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(playlist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        ImageView playIcon;

        PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.playlistImage);
            titleTextView = itemView.findViewById(R.id.playlistTitle);
            playIcon = itemView.findViewById(R.id.playIcon);
        }
    }
}