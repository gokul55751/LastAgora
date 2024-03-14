package com.example.lastagora.second;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.ChannelMediaOptions;
import com.example.lastagora.R;

public class AudioCallActivity extends AppCompatActivity {

    private final String appId = "8634b04747534d45ac4f23915914ff1b";
    private String channelName = "call";
    private String token = "007eJxTYPjodO7gH8u8eN+cvPgvi9ec0jn971am2beauzvPuD62naKmwGBhZmySZGBibmJuamySYmKamGySZmRsaWhqaWiSlmaY9PSNb2pDICPDykWzGRihEMRnYUhOzMlhYAAAQpkirw==";
    private int uid = 0;
    private boolean isJoined = false;
    private RtcEngine agoraEngine;
    private TextView infoText;
    private Button joinLeaveButton;

    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUEST_PERMISSION = {Manifest.permission.RECORD_AUDIO};
    private boolean checkSelfPermission(){
        if (ContextCompat.checkSelfPermission(this, REQUEST_PERMISSION[0])!= PackageManager.PERMISSION_GRANTED)
            return false;
        return true;
    }
    void showMessage(String message){
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private void setupVoiceSDKEngine(){
        try{
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = mRtcEventHandler;
            agoraEngine = RtcEngine.create(config);
        }catch (Exception e){

        }
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(()->infoText.setText("Remote user joined; " + uid));
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            isJoined = true;
            showMessage("Joined Channel " + channel);
            runOnUiThread(() -> infoText.setText("Waiting for a remote user to join"));
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            showMessage("Remote user offline " + uid + " " + reason);
            if(isJoined) runOnUiThread(() -> infoText.setText("waiting for a remote user to join"));
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            runOnUiThread(() -> infoText.setText("Press the button to join a channel"));
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

    public void joinLeaveChannel(View view){
        if(isJoined){
            agoraEngine.leaveChannel();
            joinLeaveButton.setText("Join");
        }else{
            joinChannel();
            joinLeaveButton.setText("Leave");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_call);
        if(!checkSelfPermission()){
            ActivityCompat.requestPermissions(this, REQUEST_PERMISSION, PERMISSION_REQ_ID);
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
        });
    }
}