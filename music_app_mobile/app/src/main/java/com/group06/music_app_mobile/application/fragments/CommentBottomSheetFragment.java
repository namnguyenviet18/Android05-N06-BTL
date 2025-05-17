package com.group06.music_app_mobile.application.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.group06.music_app_mobile.application.activities.PlayActivity;
import com.group06.music_app_mobile.application.adapters.CommentAdapter;
import com.group06.music_app_mobile.databinding.CommentViewBinding;
import com.group06.music_app_mobile.models.Comment;
import com.group06.music_app_mobile.models.Song;
import com.group06.music_app_mobile.models.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentBottomSheetFragment extends BottomSheetDialogFragment {

    private CommentViewBinding binding;

    public CommentBottomSheetFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Dùng ViewBinding
        binding = CommentViewBinding.inflate(inflater, container, false);
        setCommentCount();
        // Setup RecyclerView
        CommentAdapter commentAdapter = new CommentAdapter(
                generateComment(),
                getContext(),
                false
        );
        binding.recyclerComments.setAdapter(commentAdapter);
        // Nút gửi
        binding.btnSend.setOnClickListener(v -> {
            String text = binding.edtComment.getText().toString().trim();
            if (!text.isEmpty()) {
                // Xử lý gửi comment
                binding.edtComment.setText("");
                hideKeyboard();
            }
        });

        return binding.getRoot();
    }

    private void setCommentCount() {
        PlayActivity playActivity = getActivity() instanceof PlayActivity ? (PlayActivity) getActivity() : null;
        if(playActivity != null) {
            Song song = playActivity.getSong();
            binding.commentCount.setText(song.getCommentCount() + " comments");
        } else {
            binding.commentCount.setText("0 comments");
        }
    }

    private List<Comment> generateComment() {

        List<Comment> comments = new ArrayList<>();

        User user1 = User.builder()
                .id(12L)
                .firstName("Viet")
                .lastName("Nam")
                .enabled(true)
                .accountLocked(false)
                .build();

        User user2 = User.builder()
                .id(13L)
                .firstName("Gạch")
                .lastName("Ngói")
                .enabled(true)
                .accountLocked(false)
                .build();

        Comment comment1 = Comment.builder()
                .id(1L)
                .user(user1)
                .content("Comment của user 1")
                .isLiked(true)
                .likeCount(15)
                .createdDate(LocalDateTime.now())
                .descendantCount(10)
                .descendants(new ArrayList<>())
                .isDeleted(false)
                .showDescendants(false)
                .build();

        Comment comment2 = Comment.builder()
                .id(2L)
                .user(user1)
                .content("Comment của user 2")
                .isLiked(false)
                .likeCount(32)
                .createdDate(LocalDateTime.now())
                .descendantCount(0)
                .descendants(new ArrayList<>())
                .isDeleted(false)
                .showDescendants(false)
                .build();

        Comment comment3 = Comment.builder()
                .id(3L)
                .user(user1)
                .content("Comment của user 3")
                .isLiked(false)
                .likeCount(32)
                .createdDate(LocalDateTime.now())
                .descendantCount(0)
                .descendants(new ArrayList<>())
                .isDeleted(false)
                .showDescendants(false)
                .build();


        comment1.setDescendantCount(10);
        comment1.getDescendants().add(comment3);
        comment1.getDescendants().add(comment3);
        comment1.getDescendants().add(comment3);
        comment1.getDescendants().add(comment3);
        comment1.getDescendants().add(comment3);
        comment1.getDescendants().add(comment3);
        comment1.getDescendants().add(comment3);
        comment1.getDescendants().add(comment3);
        comment1.getDescendants().add(comment3);
        comment1.getDescendants().add(comment3);

        comments.add(comment1);
        comments.add(comment2);
        comments.add(comment2);
        comments.add(comment2);
        comments.add(comment2);
        comments.add(comment2);
        comments.add(comment2);
        comments.add(comment2);
        comments.add(comment2);
        comments.add(comment2);
        comments.add(comment2);
        comments.add(comment2);
        comments.add(comment2);
        comments.add(comment2);
        comments.add(comment2);
        comments.add(comment2);
        comments.add(comment2);

        return comments;


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
}
