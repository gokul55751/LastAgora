package com.example.lastagora;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;

public class VoiceCallActivity extends AppCompatActivity {

    private static final int PERMISSION_REQ_ID = 22;
    private final String appId = "8634b04747534d45ac4f23915914ff1b";
    private String channelName = "call";
    private String token = "007eJxTYNCdnHB9Tpp1/tIs33dc1kJhe53msUu9kzl5Yu0hNuc7PyYqMFiYGZskGZiYm5ibGpukmJgmJpukGRlbGppaGpqkpRkmHTc2TG0IZGS4uPwqAyMUgvgsDMmJOTkMDADseB57";
    private int uid = 0;
    private boolean isJoined = false;
    private RtcEngine agoraEngine;
    private TextView infoText;
    private Button joinLeaveButton;
    private static final String[] REQUESTED_PERMISSIONS = {Manifest.permission.RECORD_AUDIO};

    private boolean checkSelfPermission(){
        if (ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED)
            return false;
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);

        if(!checkSelfPermission()){
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }
        setupVoiceSDKEngine();

        joinLeaveButton = findViewById(R.id.joinLeaveButton);
        infoText = findViewById(R.id.infoText);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        agoraEngine.leaveChannel();

        new Thread(() -> {
            RtcEngine.destroy();
            agoraEngine = null;
        }).start();
    }

    private void setupVoiceSDKEngine() {
        try{
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);
        }catch (Exception e){
            Log.d("log9999", "setupVoiceSDKEngine: error " + e.getMessage());
        }
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserJoined(int uid, int elapsed) {
            // TODO: 18/10/23 Show user that you are online
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

    private void joinChannel(){
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.autoSubscribeAudio = true;
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        agoraEngine.joinChannel(token, channelName, uid, options);
    }

    public void joinLeaveChannel(View view){
        if(isJoined){
            agoraEngine.leaveChannel();
            joinLeaveButton.setText("join");
        }else{
            joinChannel();
            joinLeaveButton.setText("Leave");
        }
    }

}