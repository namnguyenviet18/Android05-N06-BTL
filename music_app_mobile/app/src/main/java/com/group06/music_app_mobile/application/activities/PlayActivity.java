package com.group06.music_app_mobile.application.activities;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.group06.music_app_mobile.R;
import com.group06.music_app_mobile.app_utils.enums.PlayMode;
import com.group06.music_app_mobile.application.adapters.PlayPagerAdapter;
import com.group06.music_app_mobile.application.fragments.LyricFragment;
import com.group06.music_app_mobile.databinding.ActivityPlayBinding;
import com.group06.music_app_mobile.models.Song;

import java.io.IOException;

import lombok.Getter;

public class PlayActivity extends AppCompatActivity {

    private ActivityPlayBinding binding;
    @Getter
    private Song song;
    private boolean isPrepared = false;
    @Getter
    private MediaPlayer mediaPlayer;

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

        song = Song.builder()
                .audioUrl("https://res.cloudinary.com/dee2s8sgk/video/upload/v1745932928/audios/Em_C%E1%BB%A7a_Ng%C3%A0y_H%C3%B4m_Qua_Lyrics_Video_t3xjgi.mp3")
                .likeCount(15)
                .coverImageUrl("https://img.tripi.vn/cdn-cgi/image/width=700,height=700/https://gcs.tripi.vn/public-tripi/tripi-feed/img/482786dLt/anh-mo-ta.png")
                .lyrics("/dee2s8sgk/raw/upload/v1745935865/audios/lyric_em_cua_ngay_home_qua_m0vjvx.json")
                .commentCount(20)
                .isPublic(true)
                .isDeleted(false)
                .isLiked(false)
                .viewCount(1500)
                .build();

        init();

        initMediaPlayer();
        playPagerAdapter = new PlayPagerAdapter(this);
        binding.playViewpager.setAdapter(playPagerAdapter);
        setUpViewPagerChange();
        setUpSeekbarOnChange();
        setUpPlayButton();
        setUpRepeatButton();
    }

    private void init() {
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
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(song.getAudioUrl());
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

        mediaPlayer.setOnCompletionListener(mp -> setPlayIcon());

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