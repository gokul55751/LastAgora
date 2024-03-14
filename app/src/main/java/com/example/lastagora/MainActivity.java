package com.example.lastagora;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lastagora.service.AudioCallingService;
import com.example.lastagora.service.VideoCallingService;

public class MainActivity extends AppCompatActivity {

    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText edtUrl = findViewById(R.id.edtUrl);
        Button button = findViewById(R.id.button);

        button.setOnClickListener(v->{
            String url = edtUrl.getText().toString();
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });

//        if(checkOverlayPermission())
//        new Handler().postDelayed(()->{
//            Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
//            startService(new Intent(this, VideoCallingService.class));
//        }, 5000);
//        else requestFloatingWindowPermission();
    }
    private void requestFloatingWindowPermission(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Screen Overlay Permission Needed");
        builder.setMessage("Enable 'Display over the App' From setting");
        builder.setPositiveButton("Open Settings", (dialogInterface, i) -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, RESULT_OK);
        });
        dialog = builder.create();
        dialog.show();
    }

    private boolean checkOverlayPermission(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            return Settings.canDrawOverlays(this);
        }
        else return true;
    }
}