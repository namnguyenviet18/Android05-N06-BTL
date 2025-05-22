package com.group06.music_app_mobile.application.activities;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.api_client.ApiClient;
import com.group06.music_app_mobile.api_client.api.SongApi;
import com.group06.music_app_mobile.app_utils.Constants;
import com.group06.music_app_mobile.app_utils.enums.DataTransferBetweenScreens;
import com.group06.music_app_mobile.app_utils.enums.PlayMode;
import com.group06.music_app_mobile.application.adapters.PlayPagerAdapter;
import com.group06.music_app_mobile.application.fragments.CommentBottomSheetFragment;
import com.group06.music_app_mobile.application.fragments.LyricFragment;
import com.group06.music_app_mobile.databinding.ActivityPlayBinding;
import com.group06.music_app_mobile.models.Song;

import java.io.IOException;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Getter
@Setter

public class PlayActivity extends AppCompatActivity {

    private ActivityPlayBinding binding;
    private Song song;

    private List<Song> songs;

    private int currentSongPosition;
    private boolean isPrepared = false;
    @Getter
    private MediaPlayer mediaPlayer;

    private SongApi songApi;

    private Handler handler = new Handler();

    private PlayPagerAdapter playPagerAdapter;

    private ViewGroup.LayoutParams selectedParams;
    private ViewGroup.LayoutParams unselectedParams;

    private PlayMode playMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        playPagerAdapter = new PlayPagerAdapter(this);
        binding.playViewpager.setAdapter(playPagerAdapter);
        if (songs.isEmpty() || song == null) {
            return;
        }
        setUpViewPagerChange();
        setUpSeekbarOnChange();
        setUpPlayButton();
        setUpRepeatButton();
        setUpCommentButton();
        setupButtonLike();
        setUpPreviousButton();
        setUpNextButton();

        binding.toolbar.setNavigationOnClickListener(view -> {
            finish();
        });
    }

    @SuppressWarnings("unchecked")
    private void init() {
        songs = (List<Song>) getIntent().getSerializableExtra(DataTransferBetweenScreens.SONG_LIST.name());
        currentSongPosition = getIntent().getIntExtra(DataTransferBetweenScreens.CURRENT_SONG_POSITION.name(), 0);
        Log.d("SIZE - INDEX", songs.size() + " - " + currentSongPosition);
        song = songs.get(currentSongPosition);
        if (songs.isEmpty() || song == null) {
            return;
        }
        displayInfo();
        songApi = ApiClient.getClient(this).create(SongApi.class);
        setNoRepeat();
        selectedParams = binding.dot0.getLayoutParams();
        unselectedParams = binding.dot1.getLayoutParams();
        if(selectedParams != null) {
            selectedParams.width = 40;
            selectedParams.height = 20;
        }
        if(unselectedParams != null) {
            unselectedParams.width = 20;
            unselectedParams.height = 20;
        }
        binding.dot0.setLayoutParams(selectedParams);
        binding.dot0.setLayoutParams(unselectedParams);
        whenDot0Selected();
        initMediaPlayer();
    }

    public void displayInfo() {
        binding.songName.setText(song.getName());
        binding.likeText.setText(String.valueOf(song.getLikeCount()) + " likes");
        binding.commentText.setText(String.valueOf(song.getCommentCount()) + " comments");
        StringBuilder songAuthor = new StringBuilder();
        if(song.getSingerName() != null && !song.getSingerName().trim().isEmpty()) {
            songAuthor.append("Performed by ").append(song.getSingerName());
        }
        if(song.getSingerName() != null && !song.getSingerName().trim().isEmpty()) {
            songAuthor.append(", composed by ")
                    .append(song.getAuthorName());
        }
        binding.songAuthor.setText(songAuthor.toString());
        if (song.isLiked()) {
            binding.likeIcon.setImageResource(R.drawable.ic_heart_fill);
        } else {
            binding.likeIcon.setImageResource(R.drawable.ic_heart_outline);
        }
    }

    private void initMediaPlayer() {
        releaseMediaPlayer();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(Constants.FILE_LOAD_ENDPOINT + song.getAudioUrl());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        mediaPlayer.setOnPreparedListener(mp -> {
            isPrepared = true;
            binding.seekBar.setMax(mediaPlayer.getDuration());
            binding.positionText.setText("0:00");
            binding.durationText.setText(getPositionText(mediaPlayer.getDuration()));
            mediaPlayer.start();
            setPauseIcon();
            updateSeekBar();
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            switch (playMode) {
                case NO_REPEAT -> setPlayIcon();
                case REPEAT_ONE -> initMediaPlayer();
                default -> next();
            }
        });

    }

    private void setupButtonLike() {
        binding.like.setOnClickListener(view -> handleClickLikeSong());
    }

    private void handleClickLikeSong() {
        if(song == null) return;
        Call<Boolean> call = songApi.handleClickLikeSong(song.getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if(response.code() == 200 && response.body() != null) {
                    song.setLiked(response.body());
                    if(song.isLiked()) {
                        song.setLikeCount(song.getLikeCount() + 1);
                    } else {
                        song.setLikeCount(song.getLikeCount() - 1);
                    }
                    displayInfo();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {

            }
        });
    }

    private void setUpPlayButton() {
        binding.playButton.setOnClickListener(view -> {
            if (isPrepared) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    setPlayIcon();
                } else {
                    mediaPlayer.start();
                    setPauseIcon();
                    updateSeekBar();
                }
            }
        });
    }

    private void setUpSeekbarOnChange() {
        binding.seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser && isPrepared) {
                            mediaPlayer.seekTo(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );
    }

    private void setUpViewPagerChange() {
        binding.playViewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if(position == 0) {
                    whenDot0Selected();
                } else {
                    whenDot1Selected();
                }
            }
        });
    }

    private void whenDot0Selected() {
        binding.dot0.setLayoutParams(selectedParams);
        binding.dot1.setLayoutParams(unselectedParams);
        binding.dot0.setCardBackgroundColor(ContextCompat.getColor(
                PlayActivity.this, R.color.light_orange));
        binding.dot1.setCardBackgroundColor(ContextCompat.getColor(
                PlayActivity.this, R.color.text_white));
    }

    private void whenDot1Selected() {
        binding.dot1.setLayoutParams(selectedParams);
        binding.dot0.setLayoutParams(unselectedParams);
        binding.dot1.setCardBackgroundColor(ContextCompat.getColor(
                PlayActivity.this, R.color.light_orange));
        binding.dot0.setCardBackgroundColor(ContextCompat.getColor(
                PlayActivity.this, R.color.text_white));
    }

    private void updateSeekBar() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPrepared && mediaPlayer.isPlaying()) {
                    binding.seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    String currentTime = getPositionText(mediaPlayer.getCurrentPosition());
                    binding.positionText.setText(currentTime);
                    LyricFragment lyricFragment = (LyricFragment) getSupportFragmentManager()
                            .findFragmentByTag("f0");
                    if (lyricFragment != null) {
                        lyricFragment.updateCurrentSentence(currentTime);
                    }
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    private void setPlayIcon() {
        binding.playIcon
                .setImageResource(R.drawable.ic_play);
    }

    private void setPauseIcon() {
        binding.playIcon
                .setImageResource(R.drawable.pause_icon);
    }

    @SuppressLint("DefaultLocale")
    public String getPositionText(int position) {
        position /= 1000;
        int minutes = position / 60;
        int seconds = position % 60;
        return String.format("%s:%02d", minutes, seconds);
    }


    private void setUpRepeatButton() {
        binding.repeatButton.setOnClickListener(v -> {
            switch (playMode) {
                case NO_REPEAT -> setRepeat();
                case REPEAT -> setRepeatOne();
                case REPEAT_ONE -> setNoRepeat();
            }
        });
    }

    private void setUpNextButton() {
        binding.nextButton.setOnClickListener(view -> {
            next();
        });
    }

    private void setUpPreviousButton() {
        binding.previousButton.setOnClickListener(view -> {
            previous();
        });
    }
    private void next() {
        if (currentSongPosition == songs.size() - 1) {
            currentSongPosition = 0;
        } else {
            currentSongPosition++;
        }
        song = songs.get(currentSongPosition);
        displayInfo();
        initMediaPlayer();

    }

    private void previous() {
        if (currentSongPosition == 0) {
            currentSongPosition = songs.size() - 1;
        } else {
            currentSongPosition--;
        }
        song = songs.get(currentSongPosition);
        displayInfo();
        initMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            isPrepared = false;
        }
    }

    private void setNoRepeat() {
        playMode = PlayMode.NO_REPEAT;
        binding.repeatButton.setImageResource(R.drawable.icon_repeat);
        binding.repeatButton
                .setColorFilter(
                    ContextCompat.getColor(
                this,
                        R.color.text_white
                ),
                PorterDuff.Mode.SRC_IN
        );
    }

    private void setRepeat() {
        playMode = PlayMode.REPEAT;
        binding.repeatButton.setImageResource(R.drawable.icon_repeat);
        binding.repeatButton
                .setColorFilter(
                    ContextCompat.getColor(
                            this,
                            R.color.light_orange
                    ),
                    PorterDuff.Mode.SRC_IN
                );
    }

    private void setRepeatOne() {
        playMode = PlayMode.REPEAT_ONE;
        binding.repeatButton.setImageResource(R.drawable.ic_repeat_one);
        binding.repeatButton
                .setColorFilter(
                        ContextCompat.getColor(
                                this,
                                R.color.light_orange
                        ),
                        PorterDuff.Mode.SRC_IN
                );
    }

    private void setUpCommentButton() {
        binding.comment.setOnClickListener(v -> {
            CommentBottomSheetFragment commentBottomSheetFragment = new CommentBottomSheetFragment();
            commentBottomSheetFragment.show(getSupportFragmentManager(), "comment_bottom_sheet");
        });
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            handler.removeCallbacksAndMessages(null);
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}