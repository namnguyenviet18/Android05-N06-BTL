package com.group06.music_app_mobile.application.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.application.events.OnLyricItemClickListener;
import com.group06.music_app_mobile.models.LyricLine;

import java.util.List;


public class LyricAdapter extends RecyclerView.Adapter<LyricAdapter.LyricViewHolder> {

    private List<LyricLine> lyrics;
    private Context context;
    private int currentSentence;

    private OnLyricItemClickListener onLyricItemClickListener;


    public void setCurrentSentence(int position) {
        int oldValue = currentSentence;
        currentSentence = position;
        if(oldValue != -1) {
            notifyItemChanged(oldValue);
        }
        if (currentSentence != -1) notifyItemChanged(currentSentence);

    }
    public LyricAdapter(
            Context context,
            List<LyricLine> lyrics,
            OnLyricItemClickListener onLyricItemClickListener,
            int currentSentence
    ) {
        this.context = context;
        this.lyrics = lyrics;
        this.currentSentence = currentSentence;
        this.onLyricItemClickListener = onLyricItemClickListener;
    }

    @NonNull
    @Override
    public LyricViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lyric_line, parent, false);
        return new LyricViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LyricViewHolder holder, int position) {
        LyricLine line = lyrics.get(position);
        holder.textView.setText(line.getText());

        if (currentSentence == position) {
            holder.textView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.light_orange));
            holder.textView.setTypeface(null, Typeface.BOLD);
        }else {
            holder.textView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_white));
            holder.textView.setTypeface(null, Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> onLyricItemClickListener.onClick(line.getTime()));
    }

    @Override
    public int getItemCount() {
        return lyrics.size();
    }

    public static class LyricViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public LyricViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.lyricText);
        }
    }
}
