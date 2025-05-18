package com.group06.music_app_mobile.application.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.CommentApi;
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

    private void hideKeyboard() {
        if (getActivity() != null && getView() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
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

    @Override
    public void displayChild(Long commentId, CommentAdapter adapter) {
        Call<List<Comment>> call = commentApi.getDescendants(commentId);
        call.enqueue(new Callback<List<Comment>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                List<Comment> child = response.body() != null ? response.body() : new ArrayList<>();
                adapter.setComments(child);
                adapter.notifyDataSetChanged();
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
    public void replyClicked(Comment comment, boolean isChildComment) {

    }
}
