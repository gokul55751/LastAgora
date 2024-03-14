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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;

public class AudioCallingService extends Service {

    private final String appId = "8634b04747534d45ac4f23915914ff1b";
    private String channelName = "call";
    private String token = "007eJxTYLjlMPVWsSPz4g1xW5WUHySfcF0SvFM0bXVYX328/jWHa0kKDBZmxiZJBibmJuamxiYpJqaJySZpRsaWhqaWhiZpaYZJ373NUxsCGRnuC5kxMEIhiM/CkJyYk8PAAAADuR3j";
    private int uid = 0;
    private boolean isJoined = false;
    private RtcEngine agoraEngine;
    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUEST_PERMISSION = {Manifest.permission.RECORD_AUDIO};
    private boolean checkSelfPermission(){
        if (ContextCompat.checkSelfPermission(this, REQUEST_PERMISSION[0])!= PackageManager.PERMISSION_GRANTED)
            return false;
        return true;
    }
    private void setupVoiceSDKEngine(){
        try{
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);
        }catch (Exception e){}
    }
    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserJoined(int uid, int elapsed) {
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            isJoined = true;
        }

        @Override
        public void onUserOffline(int uid, int reason) {
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            isJoined = false;
        }
    };
    private void joinChannel() {
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.autoSubscribeAudio = true;
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        agoraEngine.joinChannel(token, channelName, uid, options);
    }
    public void joinLeaveChannel(){
        if(isJoined){
            agoraEngine.leaveChannel();
        }else{
            joinChannel();
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

        if(!checkSelfPermission()){
            ActivityCompat.requestPermissions((Activity) getApplicationContext(), REQUEST_PERMISSION, PERMISSION_REQ_ID);
        }
        setupVoiceSDKEngine();
        new Handler().postDelayed(()->{
            joinLeaveChannel();
        }, 2500);
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
