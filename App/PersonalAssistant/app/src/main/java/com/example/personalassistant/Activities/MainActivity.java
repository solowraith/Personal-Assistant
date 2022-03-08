package com.example.personalassistant.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.personalassistant.Logic.TextParser;
import com.example.personalassistant.R;
import com.example.personalassistant.Logic.VoiceProcessing;
import com.example.personalassistant.TestFiles.voiceProcessingTEST;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity
{
    private static final int ALL_PERMISSIONS_ACCEPTED = 100;
    private static String fileName = null;
    private static final String LOG_TAG = "AudioRecordTest";
    private static VoiceProcessing vp;
    private static MediaPlayer recordingSound;
    private static MediaRecorder recorder;
    private boolean mStartRecording = true;


    // Requesting permission to RECORD_AUDIO
    private boolean permissionsAccepted = false;
    private final String [] PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.SET_ALARM, Manifest.permission.WRITE_SETTINGS};

    private PackageManager pm;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ALL_PERMISSIONS_ACCEPTED)
                permissionsAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (!permissionsAccepted)
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
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
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
        pm = this.getPackageManager();
        vp = new VoiceProcessing(MainActivity.this);

        // Record to the external cache directory for visibility
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/userInput.3gp";
        recordingSound = MediaPlayer.create(MainActivity.this, R.raw.recordingsound);

        ActivityCompat.requestPermissions(this, PERMISSIONS, ALL_PERMISSIONS_ACCEPTED);
        if(!Settings.System.canWrite(MainActivity.this))
        {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            startActivity(intent);
        }


        //Attach button and assign actions
        ImageButton FFbutton = findViewById(R.id.imageButton);
        FFbutton.setOnTouchListener(new View.OnTouchListener()
        {
            public boolean onTouch(View view, MotionEvent motionevent)
            {
                switch(motionevent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        vp.startListening(true);
                        break;

                    case MotionEvent.ACTION_UP:

                        break;

                    default:
                        break;
                }
                return false;
            }
        });

        ImageButton button = findViewById(R.id.imageButton4);
        button.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                switch(motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        performClick();

                        mStartRecording = !mStartRecording;
                        onRecord(mStartRecording);
                        vp.startListening(false);
                        break;

                    case MotionEvent.ACTION_UP:
                        onStop();

                        try
                        {
                            int reps = 0;
                            while(reps < 50)
                            {
                                Thread.sleep(10);
                                reps++;
                                if(reps > 100)
                                {
                                    Log.e("personalAssistant", "vp.getStatus() took too long to update");
                                    break;
                                }
                                if(vp.getStatus())
                                    break;
                            }

                            vp.resetStatus();

                            FileInputStream input = new FileInputStream(MainActivity.this.getExternalFilesDir(null) + "/prevCommand.txt");
                            Scanner scanner = new Scanner(input);

                            String userCommand = scanner.nextLine();
                            TextParser parser = new TextParser(userCommand, view.getContext());
                            launchIntent(parser.getIntent(), parser.getAction());
                        } catch (FileNotFoundException | InterruptedException e) {
                            e.printStackTrace();
                        }

                        break;

                    default:
                        break;
                }
                return false;
            }
            public void performClick()
            {
                recordingSound.start();
            }
        });
    }

    public void launchIntent(Intent intent, String action)
    {
        if(intent != null)
        {
            if (intent.resolveActivity(getPackageManager()) != null)
                startActivity(intent);
            else {
                System.err.printf("%naction:%s %ndata:%s", intent.getAction(), intent.getDataString());
                makeText(MainActivity.this, "Couldn't begin intent", Toast.LENGTH_LONG).show();
            }
        }
        else if (action.equals("brightness"))
                return;
        else
            makeText(MainActivity.this,"Error creating intent", Toast.LENGTH_LONG).show();
    }

    public void forInformation(View view)
    {
        Intent intent = new Intent(MainActivity.this, InformationActivity.class);
        startActivity(intent);
    }

    public void toManualEntry(View view)
    {
        Intent intent = new Intent(MainActivity.this, InputSelectorActivity.class);
        startActivity(intent);
    }

    public void toVoiceProcessing(View view)
    {
        Intent intent = new Intent(MainActivity.this, voiceProcessingTEST.class);
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