package com.example.mediaplayerapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {

    VideoView videoView;
    TextView tvStatus;
    Button btnOpenFile, btnOpenUrl, btnPlay, btnPause, btnStop, btnRestart;

    MediaPlayer audioPlayer;
    boolean isVideoMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.videoView);
        tvStatus = findViewById(R.id.tvStatus);
        btnOpenFile = findViewById(R.id.btnOpenFile);
        btnOpenUrl = findViewById(R.id.btnOpenUrl);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnStop = findViewById(R.id.btnStop);
        btnRestart = findViewById(R.id.btnRestart);

        btnOpenFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            audioPickerLauncher.launch(intent);
        });

        btnOpenUrl.setOnClickListener(v -> showUrlDialog());

        btnPlay.setOnClickListener(v -> {
            if (isVideoMode) {
                videoView.start();
                tvStatus.setText("Playing Video");
            } else if (audioPlayer != null) {
                audioPlayer.start();
                tvStatus.setText("Playing Audio");
            } else {
                Toast.makeText(MainActivity.this, "Please open a file or URL first", Toast.LENGTH_SHORT).show();
            }
        });

        btnPause.setOnClickListener(v -> {
            if (isVideoMode && videoView.isPlaying()) {
                videoView.pause();
                tvStatus.setText("Video Paused");
            } else if (!isVideoMode && audioPlayer != null && audioPlayer.isPlaying()) {
                audioPlayer.pause();
                tvStatus.setText("Audio Paused");
            }
        });

        btnStop.setOnClickListener(v -> {
            if (isVideoMode) {
                videoView.stopPlayback();
                videoView.resume();
                tvStatus.setText("Video Stopped");
            } else if (!isVideoMode && audioPlayer != null) {
                audioPlayer.stop();
                try {
                    audioPlayer.prepare();
                } catch (Exception ignored) {
                }
                tvStatus.setText("Audio Stopped");
            }
        });

        btnRestart.setOnClickListener(v -> {
            if (isVideoMode) {
                videoView.seekTo(0);
                videoView.start();
                tvStatus.setText("Video Restarted");
            } else if (!isVideoMode && audioPlayer != null) {
                audioPlayer.seekTo(0);
                audioPlayer.start();
                tvStatus.setText("Audio Restarted");
            }
        });
    }

    ActivityResultLauncher<Intent> audioPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri audioUri = result.getData().getData();
                    loadAudio(audioUri);
                }
            }
    );

    private void loadAudio(Uri uri) {
        try {
            if (audioPlayer != null) {
                audioPlayer.release();
            }
            isVideoMode = false;
            videoView.stopPlayback();

            audioPlayer = new MediaPlayer();
            audioPlayer.setDataSource(this, uri);
            audioPlayer.prepare();
            tvStatus.setText("Audio loaded. Click Play.");
        } catch (Exception ignored) {
            Toast.makeText(this, "Failed to load audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUrlDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Video URL");

        final EditText input = new EditText(this);
        input.setText("https://www.w3schools.com/html/mov_bbb.mp4");
        builder.setView(input);

        builder.setPositiveButton("Open", (dialog, which) -> {
            String url = input.getText().toString();
            loadVideo(url);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void loadVideo(String url) {
        if (audioPlayer != null) {
            audioPlayer.release();
            audioPlayer = null;
        }
        isVideoMode = true;
        videoView.setVideoURI(Uri.parse(url));
        videoView.requestFocus();
        tvStatus.setText("Video loaded. Click Play.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioPlayer != null) {
            audioPlayer.release();
        }
    }
}