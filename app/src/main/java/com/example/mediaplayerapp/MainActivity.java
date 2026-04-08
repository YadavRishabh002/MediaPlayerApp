package com.example.mediaplayerapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private TextView tvStatus;

    private MediaPlayer audioPlayer;
    private boolean isVideoMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI elements connect
        videoView = findViewById(R.id.videoView);
        tvStatus = findViewById(R.id.tvStatus);
        Button btnOpenFile = findViewById(R.id.btnOpenFile);
        Button btnOpenUrl = findViewById(R.id.btnOpenUrl);
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnPause = findViewById(R.id.btnPause);
        Button btnStop = findViewById(R.id.btnStop);
        Button btnRestart = findViewById(R.id.btnRestart);

        // Audio File Select open phone storage
        btnOpenFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*"); // only audio files visible
            audioPickerLauncher.launch(intent);
        });

        // URL input dialog box
        btnOpenUrl.setOnClickListener(v -> showUrlDialog());

        // Play Button
        btnPlay.setOnClickListener(v -> {
            if (isVideoMode) {
                videoView.start();
                tvStatus.setText(getString(R.string.status_playing_video));
            } else if (audioPlayer != null) {
                audioPlayer.start();
                tvStatus.setText(getString(R.string.status_playing_audio));
            } else {
                Toast.makeText(this, getString(R.string.msg_open_media), Toast.LENGTH_SHORT).show();
            }
        });

        // Pause Button
        btnPause.setOnClickListener(v -> {
            if (isVideoMode) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    tvStatus.setText(getString(R.string.status_video_paused));
                }
            } else if (audioPlayer != null && audioPlayer.isPlaying()) {
                audioPlayer.pause();
                tvStatus.setText(getString(R.string.status_audio_paused));
            }
        });

        // Stop Button
        btnStop.setOnClickListener(v -> {
            if (isVideoMode) {
                videoView.stopPlayback();
                tvStatus.setText(getString(R.string.status_video_stopped));
            } else if (audioPlayer != null) {
                audioPlayer.stop();
                try {
                    audioPlayer.prepare();
                } catch (Exception e) {
                    Log.e("MainActivity", "Error preparing audio", e);
                }
                tvStatus.setText(getString(R.string.status_audio_stopped));
            }
        });

        // Restart Button
        btnRestart.setOnClickListener(v -> {
            if (isVideoMode) {
                videoView.seekTo(0);
                videoView.start();
                tvStatus.setText(getString(R.string.status_video_restarted));
            } else if (audioPlayer != null) {
                audioPlayer.seekTo(0);
                audioPlayer.start();
                tvStatus.setText(getString(R.string.status_audio_restarted));
            }
        });
    }


    ActivityResultLauncher<Intent> audioPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri audioUri = result.getData().getData();
                    prepareAudio(audioUri);
                }
            }
    );

    // Audio set function
    private void prepareAudio(Uri uri) {
        try {
            if (audioPlayer != null) {
                audioPlayer.release();
            }
            isVideoMode = false;
            videoView.stopPlayback();

            audioPlayer = new MediaPlayer();
            audioPlayer.setDataSource(this, uri);
            audioPlayer.prepare();
            tvStatus.setText(getString(R.string.status_audio_loaded));
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.msg_error_audio), Toast.LENGTH_SHORT).show();
        }
    }

    // Video URL Popup dialog
    private void showUrlDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_video_url));

        final EditText input = new EditText(this);
        input.setText(getString(R.string.sample_video_url));
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.dialog_btn_open), (dialog, which) -> {
            String url = input.getText().toString();
            prepareVideo(url);
        });
        builder.setNegativeButton(getString(R.string.dialog_btn_cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Video set function
    private void prepareVideo(String url) {
        if (audioPlayer != null) {
            audioPlayer.release();
            audioPlayer = null;
        }
        isVideoMode = true;
        Uri videoUri = Uri.parse(url);
        videoView.setVideoURI(videoUri);
        videoView.requestFocus();
        tvStatus.setText(getString(R.string.status_video_loaded));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioPlayer != null) {
            audioPlayer.release();
        }
    }
}