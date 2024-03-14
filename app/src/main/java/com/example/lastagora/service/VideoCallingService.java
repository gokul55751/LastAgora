package com.example.lastagora.service;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.lastagora.R;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

public class VideoCallingService extends Service {

    private final String appId = "8634b04747534d45ac4f23915914ff1b";
    private String channelName = "call";
    private String token = "007eJxTYLjlMPVWsSPz4g1xW5WUHySfcF0SvFM0bXVYX328/jWHa0kKDBZmxiZJBibmJuamxiYpJqaJySZpRsaWhqaWhiZpaYZJ373NUxsCGRnuC5kxMEIhiM/CkJyYk8PAAAADuR3j";
    private int uid = 0;
    private boolean isJoined = false;
    private RtcEngine agoraEngine;
    private SurfaceView localSurfaceView;
    private SurfaceView remoteSurfaceView;
    private WindowManager windowManager;
    private ViewGroup floatView;
    private WindowManager.LayoutParams floatWindowLayoutParams;
    private int LAYOUT_TYPE;
    FrameLayout remoteVideoContainer;
    FrameLayout localVideoContainer;
    Button joinButton;
    Button leaveButton;
    Handler handler;

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

    private void setupVideoSDKEngine() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);
            agoraEngine.enableVideo();
        } catch (Exception e) {
        }
    }


    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserJoined(int uid, int elapsed) {
            Bundle bundle = new Bundle();
            bundle.putString("type", "remote_video");
            bundle.putInt("uid", uid);
            Message message = new Message();
            message.setData(bundle);
            handler.sendMessage(message);
//            runOnUiThread(() -> setupRemoteVideo(uid));
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            isJoined = true;
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            Bundle bundle = new Bundle();
            bundle.putString("type", "surface_visibility");
            Message message = new Message();
            message.setData(bundle);
            handler.sendMessage(message);
//            runOnUiThread(() -> remoteSurfaceView.setVisibility(View.GONE));
        }
    };

    private void setupRemoteVideo(int uid) {
        FrameLayout container = floatView.findViewById(R.id.remote_video_view_container);
        remoteSurfaceView = new SurfaceView(getBaseContext());
        remoteSurfaceView.setZOrderMediaOverlay(true);
        container.addView(remoteSurfaceView);
        agoraEngine.setupRemoteVideo(new VideoCanvas(remoteSurfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
        remoteSurfaceView.setVisibility(View.VISIBLE);
    }

    private void setupLocalVideo() {
        FrameLayout container = floatView.findViewById(R.id.local_video_view_container);
        localSurfaceView = new SurfaceView(getBaseContext());
        container.addView(localSurfaceView);
        agoraEngine.setupLocalVideo(new VideoCanvas(localSurfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }
    public void joinChannel() {
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
    public void leaveChannel() {
        if (!isJoined) {
        } else {
            agoraEngine.leaveChannel();
            if (remoteSurfaceView != null) remoteSurfaceView.setVisibility(View.GONE);
            if (localSurfaceView != null) localSurfaceView.setVisibility(View.GONE);
            isJoined = false;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startMyOwnForeground();
        else startForeground(1, new Notification());

        startFloatingView();
        setUpHandler();
        if(!checkSelfPermission()){
            ActivityCompat.requestPermissions((Activity) getApplicationContext(), REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }
        setupVideoSDKEngine();
        
    }

    private void setUpHandler() {
        handler = new Handler(getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                try{
                    String TYPE = msg.getData().getString("type");
                    switch (TYPE){
                        case "remote_video":
                            int uid = msg.getData().getInt("uid");
                            setupRemoteVideo(uid);
                            break;
                        case "surface_visibility":
                            remoteSurfaceView.setVisibility(View.GONE);
                            break;
                    }
                }catch (Exception e){

                }
            }
        };
    }

    private void startFloatingView() {

        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        floatView = (ViewGroup) inflater.inflate(R.layout.video_calling_layout, null);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_TOAST;
        floatWindowLayoutParams = new WindowManager.LayoutParams(
                (int) Math.round(width*0.75f),
                (int) Math.round(height*0.75f),
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        floatWindowLayoutParams.gravity = Gravity.CENTER;
        floatWindowLayoutParams.x = 0;
        floatWindowLayoutParams.y = 0;
        windowManager.addView(floatView, floatWindowLayoutParams);

        remoteVideoContainer = floatView.findViewById(R.id.remote_video_view_container);
        localVideoContainer = floatView.findViewById(R.id.local_video_view_container);
        joinButton = floatView.findViewById(R.id.JoinButton);
        leaveButton = floatView.findViewById(R.id.LeaveButton);

        joinButton.setOnClickListener((v)->{
            joinChannel();
        });
        leaveButton.setOnClickListener((v)->{
            leaveChannel();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        agoraEngine.stopPreview();
        agoraEngine.leaveChannel();
        new Thread(()-> {
            RtcEngine.destroy();
            agoraEngine = null;
        }).start();
    }

    private void startMyOwnForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
            String channelName = "My Background Service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);
        }
    }
}
