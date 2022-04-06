package com.example.personalassistant.Activities;

import static android.widget.Toast.makeText;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.personalassistant.Logic.TextParser;
import com.example.personalassistant.Logic.VoiceProcessing;
import com.example.personalassistant.R;
import com.google.android.material.chip.Chip;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int ALL_PERMISSIONS_ACCEPTED = 100;
    private static final String LOG_TAG_Audio = "AudioRecording";
    private static final String LOG_TAG_INTENT = "IntentLauncher";
    public static MutableLiveData<Boolean> recogInit = new MutableLiveData<>(false);
    public static MutableLiveData<TextParser> textParser = new MutableLiveData<>();
    protected static VoiceProcessing vp;
    private static final MutableLiveData<Boolean> ttsSetUp = new MutableLiveData<>(false);
    private static String fileName = null;
    private static MediaPlayer recordingSound;
    private static MediaRecorder recorder;
    private static TextToSpeech tts;
    private final String[] PERMISSIONS = {Manifest.permission.RECORD_AUDIO, Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.SET_ALARM, Manifest.permission.WRITE_SETTINGS};
    private boolean mStartRecording = true;
    // Requesting permission to RECORD_AUDIO
    private PackageManager pm;
    private boolean permissionsAccepted = false;

    /*
    Using PERMISSIONS, is given to the Android Permissions Manager which verifies that the applicaiton
        has access to the requested permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ALL_PERMISSIONS_ACCEPTED)
            permissionsAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (!permissionsAccepted)
            finish();
    }

    private void onRecord(boolean start) {
        if (start)
            startRecording();
        else
            onStop();
    }

    /*
    Begins recording a .3GP file of the user's voice command for transparency to the user
     */
    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG_Audio, "prepare() failed");
        }

        recorder.start();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recogInit.setValue(false);

        pm = this.getPackageManager();

        // sets filepath of the recording to the external cache directory for transparency
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/userInput.3gp";

        //Gets sound resource
        recordingSound = MediaPlayer.create(MainActivity.this, R.raw.recordingsound);

        //Beginning of requesting permissions
        ActivityCompat.requestPermissions(this, PERMISSIONS, ALL_PERMISSIONS_ACCEPTED);

        //Due to writing to system settings being handled differently by Android OS,
        //the user must explicitly change the app's setting manually, therefore
        //here the user is alerted and prompted to do so.
        if (!Settings.System.canWrite(MainActivity.this)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("Please allow the app to manage system settings");
            alertDialogBuilder.setCancelable(false);

            alertDialogBuilder.setPositiveButton("Ok",
                    (dialog, id) ->
                    {
                        dialog.dismiss();

                        //On accepting, user is taken to the Settings screen to update the permission
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        startActivity(intent);
                    });
            alertDialogBuilder.setNegativeButton("Cancel",
                    (dialog, id) ->
                    {
                        Log.e("MainActivity.alertDialog.Negative", "User denied access to permissions");

                        //On denial, application closes due to lack of permissions
                        makeText(MainActivity.this, "Please allow the app to manage system settings",
                                Toast.LENGTH_LONG).show();
                        dialog.cancel();
                        finish();
                    });

            AlertDialog dialog = alertDialogBuilder.create();
            dialog.show();
        }

        tts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                }
                ttsSetUp.setValue(true);
            }
        });

        vp = new VoiceProcessing(MainActivity.this);

        //Grabs references to all relevant buttons on screen
        Chip toggleFF = findViewById(R.id.chip4);
        ImageButton button = findViewById(R.id.imageButton4);
        ProgressBar progress = findViewById(R.id.progressBar);
        TextView text = findViewById(R.id.textView);
        ImageButton info = findViewById(R.id.imageButton5);

        //Set toggled states for freeform speech
        toggleFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vp.toggle = !vp.toggle;
                if (vp.toggle) {
                    recogInit.setValue(true);
                    toggleFF.setText(R.string.FFOn);
                    toggleFF.setTextColor(getResources().getColor(R.color.white));
                    toggleFF.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                } else {
                    toggleFF.setText(R.string.FFOff);
                    toggleFF.setTextColor(getResources().getColor(R.color.black));
                    toggleFF.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                }
            }
        });

        //Hides all relevant buttons while the speech recognizer loads language model
        progress.setVisibility(View.VISIBLE);
        button.setVisibility(View.GONE);
        info.setVisibility(View.GONE);
        toggleFF.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
        text.setText(R.string.loading);

        //Sets button states for voice recording button
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: //When user begins holding the button down
                        performClick();

                        //Toggles the state of global boolean, prevents the recorder from being
                        //started while already recording.
                        mStartRecording = !mStartRecording;
                        onRecord(mStartRecording);
                        vp.startListening(false);
                        break;

                    case MotionEvent.ACTION_UP:
                        onStop();

                        break;

                    default:
                        break;
                }
                return false;
            }

            public void performClick() {
                recordingSound.start();
            }
        });

        //This boolean is updated by voiceProcessing when the recognizer has been fully loaded,
        //On update to the boolean's value to true, unhides the previously hidden buttons.
        recogInit.observe(MainActivity.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    button.setVisibility(View.VISIBLE);
                    text.setText(R.string.voice_commands);
                    info.setVisibility(View.VISIBLE);
                    toggleFF.setVisibility(View.VISIBLE);
                    button.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);

                    //TODO: Test passive Listening Service
                    startService(new Intent(MainActivity.this, ListeningService.class));
                }
            }
        });

        textParser.observe(MainActivity.this, new Observer<TextParser>() {
            @Override
            public void onChanged(TextParser textParser) {
                if (textParser.getIntent() != null)
                    launchIntent(textParser.getIntent(), textParser.getAction());
            }
        });

        /* TODO: Work on TTS Capabilities
        ttsSetUp.observe(MainActivity.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean)
                {
                    TextParser temp = new TextParser("set an alarm for two o'clock pm", MainActivity.this, false);
                    launchIntent(temp.getIntent(), temp.getAction());
                }
            }
        });
        */
    }

    public void launchIntent(Intent intent, String action) {
        //No need for starting an intent when the command was to change brightness
        if (action.equals("brightness"))
            return;

        if (intent != null) {
            if (intent.resolveActivity(getPackageManager()) != null) {
                /* TODO: Work on TTS Capabilities
                switch(action)
                {
                    case "alarm":
                        tts.speak("Alarm set now for " + intent.getIntExtra(AlarmClock.EXTRA_HOUR,0) + " "
                                + intent.getLongExtra(AlarmClock.EXTRA_MINUTES,0) + " " + (intent.getBooleanExtra(AlarmClock.EXTRA_IS_PM,false)? "am": "pm")
                                ,TextToSpeech.QUEUE_FLUSH,null);
                        break;
                    case "reminder":
                        break;
                    case "timer":
                        break;
                }
                */

                startActivity(intent);
            } else {
                Log.e(LOG_TAG_INTENT, "\naction:" + intent.getAction() + "\nData:" + intent.getDataString());
                System.err.printf("%naction:%s %ndata:%s", intent.getAction(), intent.getDataString());
                makeText(MainActivity.this, "Couldn't launch intent", Toast.LENGTH_LONG).show();
            }
        } else
            makeText(MainActivity.this, "Error launching intent", Toast.LENGTH_LONG).show();
    }

    public void forInformation(View view) {
        Intent intent = new Intent(MainActivity.this, InformationActivity.class);
        startActivity(intent);
    }

    public void toManualEntry(View view) {
        Intent intent = new Intent(MainActivity.this, InputSelectorActivity.class);
        startActivity(intent);
    }

    /*
    Handles deletion of the audio recorder when not in use
     */
    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }
}