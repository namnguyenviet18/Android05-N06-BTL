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
import com.group06.music_app_mobile.app_utils.ReplyCommentHelper;
import com.group06.music_app_mobile.application.events.OnExpandChildCommentListener;
import com.group06.music_app_mobile.application.events.OnLikeClickListener;
import com.group06.music_app_mobile.application.events.OnReplyClickListener;
import com.group06.music_app_mobile.databinding.ItemCommentBinding;
import com.group06.music_app_mobile.models.Comment;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    @Getter
    @Setter
    private List<Comment> comments;
    private Context context;
    private boolean isChildComment;

    private OnLikeClickListener onLikeClickListener;

    private OnExpandChildCommentListener onExpandChildCommentListener;

    private OnReplyClickListener onReplyClickListener;
    private Comment root;

    private int rootPosition = -1;;

    public CommentAdapter(
            List<Comment> comments,
            Context context,
            boolean isChildComment,
            OnLikeClickListener onLikeClickListener,
            OnExpandChildCommentListener onExpandChildCommentListener,
            OnReplyClickListener onReplyClickListener
    ) {
        this.comments = comments;
        this.context = context;
        this.isChildComment = isChildComment;
        this.onLikeClickListener = onLikeClickListener;
        this.onExpandChildCommentListener = onExpandChildCommentListener;
        this.onReplyClickListener = onReplyClickListener;
    }

    public CommentAdapter(
            List<Comment> comments,
            Context context,
            boolean isChildComment,
            OnLikeClickListener onLikeClickListener,
            OnExpandChildCommentListener onExpandChildCommentListener,
            OnReplyClickListener onReplyClickListener,
            Comment root,
            int rootPosition
    ) {
        this.comments = comments;
        this.context = context;
        this.isChildComment = isChildComment;
        this.onLikeClickListener = onLikeClickListener;
        this.onExpandChildCommentListener = onExpandChildCommentListener;
        this.onReplyClickListener = onReplyClickListener;
        this.root = root;
        this.rootPosition = rootPosition;
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

        setExpandChildState(holder.binding, comment, position);

        displayCommentInfo(holder, comment);

        setupExpandChild(holder.binding, comment, position);

        setupHandleLike(holder.binding, comment.getId());

        setupReply(holder.binding, comment, position);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }


    private void setupExpandChild(ItemCommentBinding binding, Comment comment, int position) {
        binding.displayFeedback.setOnClickListener(view -> {
            comment.setShowDescendants(!comment.isShowDescendants());
            setExpandChildState(binding, comment, position);
        });
    }
    private void setupHandleLike(ItemCommentBinding binding, Long commentId) {
        binding.commentLikeButton.setOnClickListener(view -> {
            onLikeClickListener.handleLike(commentId, this);
        });
    }

    private void setupReply(ItemCommentBinding binding, Comment comment, int position) {
        binding.feedbackButton.setOnClickListener(view -> {
            onReplyClickListener.replyClicked(ReplyCommentHelper.builder()
                    .comment(root != null ? root : comment)
                    .adapter(this)
                    .isChildComment(isChildComment)
                    .position(rootPosition == -1 ? position : rootPosition)
                    .build());
        });
    }


    private void setExpandChildState(ItemCommentBinding binding, Comment comment, int position) {
        if(isChildComment) {
            return;
        }
        if(!comment.isShowDescendants()) {
            binding.recyclerViewFeedback.setAdapter(null);
            binding.recyclerViewFeedback.setVisibility(View.GONE);
            binding.viewFeedback.setText("View " + comment.getDescendantCount() + " responses");
        } else {
            binding.recyclerViewFeedback.setVisibility(View.VISIBLE);
            binding.viewFeedback.setText("Hide responses");
            CommentAdapter childAdapter = new CommentAdapter( // LOCAL adapter
                    comment.getDescendants(),
                    context,
                    true,
                    onLikeClickListener,
                    onExpandChildCommentListener,
                    onReplyClickListener,
                    comment,
                    position
            );
            binding.recyclerViewFeedback.setAdapter(childAdapter);
            if(comment.getDescendantCount() > comment.getDescendants().size()) {
                onExpandChildCommentListener.displayChild(comment, position);
            }
        }
    }
    private void displayCommentInfo(CommentViewHolder holder, Comment comment) {
        Glide.with(context)
                .load(comment.getUser().getAvatarUrl())
                .centerCrop()
                .placeholder(R.drawable.ic_user_fill)
                .error(R.drawable.ic_user_fill)
                .into(holder.binding.commentAvatar);
        setAvatarSize(holder.binding);
        String name = comment.getUser().getFullName();
        if(comment.getParent() != null) {
            name += " > " + comment.getParent().getUser().getFullName();
        }
        holder.binding.commentName.setText(name);
        holder.binding.commentContent.setText(comment.getContent());
        holder.binding.commentTime.setText(AppUtils.getTimeAgo(LocalDateTime.parse(comment.getCreatedDate())));
        if(comment.getLikeCount() > 0) {
            holder.binding.commentLikeText.setText(String.valueOf(comment.getLikeCount()));
        }else {
            holder.binding.commentLikeText.setText("  ");
        }
        if (comment.isLiked()) {
            holder.binding.commentLikeButton.setImageResource(R.drawable.ic_heart_fill);
        } else {
            holder.binding.commentLikeButton.setImageResource(R.drawable.ic_heart_outline);
        }
        if(!comment.isShowDescendants()) {
            holder.binding.viewFeedback.setText("View " + comment.getDescendantCount() + " responses");
        } else {
            holder.binding.viewFeedback.setText("Hide responses");
        }
        if(comment.getDescendantCount() <= 0) {
            holder.binding.displayFeedback.setVisibility(View.GONE);
        } else {
            holder.binding.displayFeedback.setVisibility(View.VISIBLE);
        }
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
