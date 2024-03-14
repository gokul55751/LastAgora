package com.example.lastagora.second;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.lastagora.R;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.content.Intent;
import android.os.Build;
import android.util.DisplayMetrics;

import io.agora.rtc2.ScreenCaptureParameters;

public class ScreenSharingActivity extends AppCompatActivity {


    private final String appId = "8634b04747534d45ac4f23915914ff1b";
    private String channelName = "call";
    private String token = "007eJxTYLjzK6tkunfbsZffz77uDnLa3pPz7KGLoFCxxNldVh5reZMUGCzMjE2SDEzMTcxNjU1STEwTk03SjIwtDU0tDU3S0gyT7lo4pzYEMjJErF7HyMgAgSA+C0NyYk4OAwMAwwIgIw==";
    private int uid = 0;
    private boolean isJoined = false;
    private RtcEngine agoraEngine;
    private SurfaceView localSurfaceView;
    private SurfaceView remoteSurfaceView;
    // Volume Control
    private SeekBar volumeSeekBar;
    private CheckBox muteCheckBox;
    private int volume = 50;
    private int remoteUid = 0; // Stores the uid of the remote user

    // Screen sharing
    private final int DEFAULT_SHARE_FRAME_RATE = 10;
    private boolean isSharingScreen = false;
    private Intent fgServiceIntent;


    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS =
            {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
            };

    private boolean checkSelfPermission()
    {
        if (ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) !=  PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[1]) !=  PackageManager.PERMISSION_GRANTED)
        {
            return false;
        }
        return true;
    }
    void showMessage(String message) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }

    private void setupVideoSDKEngine() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);
            agoraEngine.enableVideo();
        } catch (Exception e) {
            showMessage(e.toString());
        }
    }


    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserJoined(int uid, int elapsed) {
            showMessage("Remote user joined " + uid);
            runOnUiThread(() -> setupRemoteVideo(uid));
            remoteUid = uid;
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            isJoined = true;
            showMessage("Joined Channel " + channel);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            showMessage("Remote user offline " + uid + " " + reason);
            runOnUiThread(() -> remoteSurfaceView.setVisibility(View.GONE));
        }
    };
    private void setupRemoteVideo(int uid) {
        FrameLayout container = findViewById(R.id.remote_video_view_container);
        remoteSurfaceView = new SurfaceView(getBaseContext());
        remoteSurfaceView.setZOrderMediaOverlay(true);
        container.addView(remoteSurfaceView);
        agoraEngine.setupRemoteVideo(new VideoCanvas(remoteSurfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
        remoteSurfaceView.setVisibility(View.VISIBLE);
    }

    private void setupLocalVideo() {
        FrameLayout container = findViewById(R.id.local_video_view_container);
        localSurfaceView = new SurfaceView(getBaseContext());
        container.addView(localSurfaceView);
        agoraEngine.setupLocalVideo(new VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }


    public void joinChannel(View view) {
        if (checkSelfPermission()) {
            ChannelMediaOptions options = new ChannelMediaOptions();
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
            setupLocalVideo();
            localSurfaceView.setVisibility(View.VISIBLE);
            agoraEngine.startPreview();
            agoraEngine.joinChannel(token, channelName, uid, options);
        } else {
            Toast.makeText(getApplicationContext(), "Permissions was not granted", Toast.LENGTH_SHORT).show();
        }
    }

    public void leaveChannel(View view) {
        if (!isJoined) {
            showMessage("Join a channel first");
        } else {
            agoraEngine.leaveChannel();
            showMessage("You left the channel");
            if (remoteSurfaceView != null) remoteSurfaceView.setVisibility(View.GONE);
            if (localSurfaceView != null) localSurfaceView.setVisibility(View.GONE);
            isJoined = false;
        }
    }
    public void shareScreen(View view) {
        Button sharingButton = (Button) view;

        if (!isSharingScreen) { // Start sharing
            // Ensure that your Android version is Lollipop or higher.
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    fgServiceIntent = new Intent(this, ScreenSharingActivity.class);
                    startForegroundService(fgServiceIntent);
                }
                // Get the screen metrics
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);

                // Set screen capture parameters
                ScreenCaptureParameters screenCaptureParameters = new ScreenCaptureParameters();
                screenCaptureParameters.captureVideo = true;
                screenCaptureParameters.videoCaptureParameters.width = metrics.widthPixels;
                screenCaptureParameters.videoCaptureParameters.height = metrics.heightPixels;
                screenCaptureParameters.videoCaptureParameters.framerate = DEFAULT_SHARE_FRAME_RATE;
                screenCaptureParameters.captureAudio = true;
                screenCaptureParameters.audioCaptureParameters.captureSignalVolume = 50;

                // Start screen sharing
                agoraEngine.startScreenCapture(screenCaptureParameters);
                isSharingScreen = true;
                startScreenSharePreview();
                // Update channel media options to publish the screen sharing video stream
                updateMediaPublishOptions(true);
                sharingButton.setText("Stop Screen Sharing");
            }
        } else { // Stop sharing
            agoraEngine.stopScreenCapture();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (fgServiceIntent!=null) stopService(fgServiceIntent);
            }
            isSharingScreen = false;
            sharingButton.setText("Start Screen Sharing");

            // Restore camera and microphone publishing
            updateMediaPublishOptions(false);
            setupLocalVideo();
        }
    }

    private void startScreenSharePreview() {
        // Create render view by RtcEngine
        FrameLayout container = findViewById(R.id.local_video_view_container);
        SurfaceView surfaceView = new SurfaceView(getBaseContext());
        if (container.getChildCount() > 0) {
            container.removeAllViews();
        }
        // Add SurfaceView to the local FrameLayout
        container.addView(surfaceView,
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        // Setup local video to render your local camera preview
        agoraEngine.setupLocalVideo(new VideoCanvas(surfaceView, Constants.RENDER_MODE_FIT, 0));

        agoraEngine.startPreview(Constants.VideoSourceType.VIDEO_SOURCE_SCREEN_PRIMARY);
    }
    void updateMediaPublishOptions(boolean publishScreen) {
        ChannelMediaOptions mediaOptions = new ChannelMediaOptions();
        mediaOptions.publishCameraTrack = !publishScreen;
        mediaOptions.publishMicrophoneTrack = !publishScreen;
        mediaOptions.publishScreenCaptureVideo = publishScreen;
        mediaOptions.publishScreenCaptureAudio = publishScreen;
        agoraEngine.updateChannelMediaOptions(mediaOptions);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_sharing);
        if(!checkSelfPermission()){
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }
        setupVideoSDKEngine();

        volumeSeekBar = (SeekBar)findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volume = progress;
                agoraEngine.adjustRecordingSignalVolume(volume);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                //Required to implement OnSeekBarChangeListener
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                //Required to implement OnSeekBarChangeListener
            }
        });

        muteCheckBox = (CheckBox) findViewById(R.id.muteCheckBox);
        muteCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                agoraEngine.muteRemoteAudioStream(remoteUid, isChecked);
            }
        });

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        agoraEngine.stopPreview();
        agoraEngine.leaveChannel();
        new Thread(()-> {
            RtcEngine.destroy();
            agoraEngine = null;
        }).start();
    }
}