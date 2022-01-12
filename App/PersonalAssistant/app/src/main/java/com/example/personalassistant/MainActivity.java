package com.example.personalassistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;
    private static final String LOG_TAG = "AudioRecordTest";
    private MediaRecorder recorder = null;
    private boolean mStartRecording = true;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private final String [] PERMISSIONS = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION)
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (!permissionToRecordAccepted)
            finish();
    }

    private void onRecord(boolean start)
    {
        if (start)
            startRecording();
        else
            onStop();
    }

    private void startRecording()
    {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try
        {
            recorder.prepare();
        }
        catch (IOException e)
        {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Record to the external cache directory for visibility
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/userInput.3gp";

        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_RECORD_AUDIO_PERMISSION);

        //Attach button and assign actions
        ImageButton button = findViewById(R.id.imageButton4);
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch(motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        performClick();
                        onRecord(mStartRecording);
                        mStartRecording = !mStartRecording;
                        break;

                    case MotionEvent.ACTION_UP:
                        onStop();
                        break;

                    default:
                        break;
                }
                return false;
            }
            public void performClick()
            {
                final MediaPlayer RECORDING = MediaPlayer.create(MainActivity.this, R.raw.recordingsound);
                RECORDING.start();
            }
        });
    }

    public void forInformation(View view)
    {
        Intent intent = new Intent(MainActivity.this, InformationActivity.class);
        startActivity(intent);
    }

    public void toManualEntry(View view)
    {
        Intent intent = new Intent(MainActivity.this, InputSelector.class);
        startActivity(intent);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (recorder != null)
        {
            recorder.release();
            recorder = null;
        }
    }
}