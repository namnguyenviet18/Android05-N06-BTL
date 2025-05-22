package com.group06.music_app_mobile.application.adapters;

import static com.group06.music_app_mobile.app_utils.ServerDestination.SERVER_HOST;
import static com.group06.music_app_mobile.app_utils.ServerDestination.SERVER_PORT;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.models.Playlist;

import java.util.List;


public class PlaylistDialogAdapter extends ArrayAdapter<Playlist> {
    private final Context context;
    private final List<Playlist> playlists;

    public PlaylistDialogAdapter(Context context, List<Playlist> playlists) {
        super(context, R.layout.dialog_list_item, playlists);
        this.context = context;
        this.playlists = playlists;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Playlist playlist = playlists.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.dialog_list_item, parent, false);
        }

        ImageView iconView = convertView.findViewById(R.id.icon_item);
        TextView textView = convertView.findViewById(R.id.text_item);
        ImageView globalIconView = convertView.findViewById(R.id.global_icon); // Optional: nếu bạn cần icon phụ

        textView.setText(playlist.getName());

        String fullImageUrl = "http://" + SERVER_HOST + ":" + SERVER_PORT + "/api/v1/song/file/load?fullUrl=" + playlist.getCoverImageUrl();
        // Load ảnh cover bằng Glide
        Glide.with(context)
                .load(fullImageUrl) // URL ảnh
                .placeholder(R.drawable.ana) // ảnh tạm thời khi loading
                .error(R.drawable.ana) // ảnh nếu lỗi
                .into(iconView);

        // Nếu bạn muốn ẩn globalIconView:
        globalIconView.setVisibility(View.GONE); // hoặc gán icon tuỳ ý

        return convertView;
    }
}

