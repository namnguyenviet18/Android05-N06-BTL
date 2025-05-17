package com.group06.music_app_mobile.application.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.app_utils.AppUtils;
import com.group06.music_app_mobile.databinding.ItemCommentBinding;
import com.group06.music_app_mobile.models.Comment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> comments;
    private Context context;
    private boolean isChildComment;


    public CommentAdapter(
            List<Comment> comments,
            Context context,
            boolean isChildComment
    ) {
        this.comments = comments;
        this.context = context;
        this.isChildComment = isChildComment;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentBinding binding = ItemCommentBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new CommentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        Glide.with(context)
                .load(comment.getUser().getAvatarUrl())
                .centerCrop()
                .placeholder(R.drawable.ic_user_fill)
                .error(R.drawable.ic_user_fill)
                .into(holder.binding.commentAvatar);
        setAvatarSize(holder.binding);
        holder.binding.getRoot().setId(Math.toIntExact(comment.getId()));
        holder.binding.commentName.setText(comment.getUser().getFullName());
        holder.binding.commentContent.setText(comment.getContent());
        holder.binding.commentTime.setText(AppUtils.getTimeAgo(comment.getCreatedDate()));
        if (comment.isLiked()) {
            holder.binding.commentLikeButton.setImageResource(R.drawable.ic_heart_fill);
        } else {
            holder.binding.commentLikeButton.setImageResource(R.drawable.ic_heart_outline);
        }
        holder.binding.commentLikeText.setText(String.valueOf(comment.getLikeCount()));
        if(comment.getDescendantCount() <= 0) {
            holder.binding.displayFeedback.setVisibility(View.GONE);
        } else {
            holder.binding.displayFeedback.setVisibility(View.VISIBLE);
        }

        holder.binding.displayFeedback.setOnClickListener(view -> {
            comment.setShowDescendants(!comment.isShowDescendants());
            if(!comment.isShowDescendants()) {
                holder.binding.recyclerViewFeedback.setVisibility(View.GONE);
                holder.binding.viewFeedback.setText("View " + comment.getDescendantCount() + " responses");
            } else {
                holder.binding.recyclerViewFeedback.setVisibility(View.VISIBLE);
                holder.binding.viewFeedback.setText("Hide responses");
                CommentAdapter commentAdapter = new CommentAdapter(
                        comment.getDescendants(),
                        context,
                        true
                );
                holder.binding.recyclerViewFeedback.setAdapter(commentAdapter);
            }
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }


    private void setAvatarSize(ItemCommentBinding binding) {
        float scale = context.getResources().getDisplayMetrics().density;
        ViewGroup.LayoutParams avatarParams = binding.avatarHolder.getLayoutParams();
        if(isChildComment) {
            avatarParams.width = (int) (30 * scale + 0.5f);
            avatarParams.height = (int) (30 * scale + 0.5f);
        } else {
            avatarParams.width = (int) (40 * scale + 0.5f);
            avatarParams.height = (int) (40 * scale + 0.5f);
        }
        binding.avatarHolder.setLayoutParams(avatarParams);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        ItemCommentBinding binding;
        public CommentViewHolder(@NonNull ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
