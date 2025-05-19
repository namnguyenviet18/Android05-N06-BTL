package com.group06.music_app_mobile.application.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.CommentApi;
import com.group06.music_app_mobile.api_client.requests.CommentRequest;
import com.group06.music_app_mobile.app_utils.ReplyCommentHelper;
import com.group06.music_app_mobile.application.activities.PlayActivity;
import com.group06.music_app_mobile.application.adapters.CommentAdapter;
import com.group06.music_app_mobile.application.events.OnExpandChildCommentListener;
import com.group06.music_app_mobile.application.events.OnLikeClickListener;
import com.group06.music_app_mobile.application.events.OnReplyClickListener;
import com.group06.music_app_mobile.databinding.CommentViewBinding;
import com.group06.music_app_mobile.models.Comment;
import com.group06.music_app_mobile.models.Song;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentBottomSheetFragment
        extends
        BottomSheetDialogFragment
        implements
        OnExpandChildCommentListener,
        OnLikeClickListener,
        OnReplyClickListener
{
    private CommentViewBinding binding;
    private CommentApi commentApi;
    private PlayActivity playActivity;
    private List<Comment> comments;
    private CommentAdapter commentAdapter;
    private ReplyCommentHelper replyCommentHelper;


    public CommentBottomSheetFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = CommentViewBinding.inflate(inflater, container, false);
        commentApi = ApiClient.getClient(requireContext()).create(CommentApi.class);
        playActivity = getActivity() instanceof PlayActivity ? (PlayActivity) getActivity() : null;
        setCommentCount();
        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(
                comments,
                getContext(),
                false,
                this,
                this,
                this
        );
        binding.recyclerComments.setAdapter(commentAdapter);
        getCommentsBySong();
        setReplyToViewState();
        setupCancelReplyTo();
        setupCommentInputWatcher();
        setupSendComment();
        return binding.getRoot();
    }

    private void getCommentsBySong() {
        if(playActivity == null) {
            return;
        }
        Song song = playActivity.getSong();
        if(song == null) {
            return;
        }
        Log.d("SONG ID: ", song.getId().toString());
        Call<List<Comment>> call = commentApi.getCommentsBySong(song.getId());
        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                comments.clear();
                comments.addAll(response.body() != null ? response.body() : new ArrayList<>());
                Log.d("COMMENTS SIZE: ", response.body().size() + "");
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                Log.e("CommentBottomSheetFragment", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void setupSendComment() {
        binding.btnSend.setOnClickListener(view -> {
            sendComment();
        });
    }

    private void sendComment() {
        if (playActivity == null) {
            return;
        }
        Song song = playActivity.getSong();
        if(song == null) {
            return;
        }
        CommentRequest commentRequest = CommentRequest.builder()
                .content(binding.edtComment.getText().toString().trim())
                .songId(song.getId())
                .build();
        if(replyCommentHelper != null && replyCommentHelper.getComment() != null) {
            commentRequest.setParentId(replyCommentHelper.getComment().getId());
        }
        Call<Comment> call = commentApi.createComment(commentRequest);
        call.enqueue(new Callback<Comment>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                if(response.isSuccessful()) {
                    Comment newComment = response.body();
                    if(newComment == null) return;
                    if(replyCommentHelper == null) {
                        Log.d("HI", "vao day 1");
                        comments.add(0, newComment);
                        commentAdapter.notifyItemInserted(0);
                        binding.recyclerComments.scrollToPosition(0);
                    } else {
                        Comment comment = replyCommentHelper.getComment();
                        comment.setShowDescendants(true);
                        comment.getDescendants().add(newComment);
                        comment.setDescendantCount(comment.getDescendantCount() + 1);
                        commentAdapter.notifyItemChanged(replyCommentHelper.getPosition());
                        binding.edtComment.setText("");
                        replyCommentHelper = null;
                        setReplyToViewState();
                    }
                }
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {

            }
        });

    }

    @Override
    public void displayChild(Comment comment, int position) {
        Call<List<Comment>> call = commentApi.getDescendants(comment.getId());
        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                List<Comment> child = response.body() != null ? response.body() : new ArrayList<>();
                for (Comment c : child) {
                    if (!comment.getDescendants().contains(c)) {
                        comment.getDescendants().add(c);
                    }
                }
                comment.setDescendantCount(comment.getDescendants().size());
                commentAdapter.notifyItemChanged(position);
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {

            }
        });
    }

    @Override
    public void handleLike(Long id, CommentAdapter adapter) {
        Call<Void> call = commentApi.clickLikeComment(id);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.code() == 200) {
                    for(int i = 0; i < adapter.getComments().size(); i++) {
                        Comment comment = adapter.getComments().get(i);
                        if(Objects.equals(comment.getId(), id)) {
                            updateCommentLikeState(comment, id);
                            adapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {

            }
        });
    }


    @Override
    public void replyClicked(ReplyCommentHelper replyCommentHelper) {
        this.replyCommentHelper = replyCommentHelper;
        binding.edtComment.requestFocus();
        setReplyToViewState();
    }

    private void setReplyToViewState() {
        if(replyCommentHelper == null || replyCommentHelper.getComment() == null) {
            binding.replyTo.setVisibility(View.GONE);
            binding.edtComment.clearFocus();
            hideKeyboard();
        } else {
            binding.replyTo.setVisibility(View.VISIBLE);
            binding.replyToName.setText(replyCommentHelper.getComment().getUser().getFullName());
            binding.edtComment.requestFocus();
            showKeyboard(binding.edtComment);
        }
    }

    private void setupCancelReplyTo() {
        binding.cancelReplyTo.setOnClickListener(view -> {
            replyCommentHelper = null;
            setReplyToViewState();
        });
    }

    private void updateCommentLikeState(Comment comment, Long id) {
        if(!Objects.equals(comment.getId(), id)) {
            return;
        }
        if(comment.isLiked()) {
            comment.setLikeCount(comment.getLikeCount() - 1);
        } else {
            comment.setLikeCount(comment.getLikeCount() + 1);
        }
        comment.setLiked(!comment.isLiked());
    }

    private void setCommentCount() {
        if(playActivity != null) {
            Song song = playActivity.getSong();
            binding.commentCount.setText(song.getCommentCount() + " comments");
        } else {
            binding.commentCount.setText("0 comments");
        }
    }

    private void setupCommentInputWatcher() {
        binding.edtComment.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = s != null && !s.toString().trim().isEmpty();
                binding.btnSend.setClickable(hasText);
                binding.btnSend.setEnabled(hasText);
                binding.btnSend.setColorFilter(ContextCompat.getColor(
                        requireContext(),
                        hasText ? R.color.teal_700 : R.color.gray_light  // bạn chọn màu phù hợp
                ));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void hideKeyboard() {
        if (getActivity() != null && getView() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    private void showKeyboard(View targetView) {
        if (targetView == null) return;
        InputMethodManager imm =
                (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(targetView, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.requestLayout();

                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
