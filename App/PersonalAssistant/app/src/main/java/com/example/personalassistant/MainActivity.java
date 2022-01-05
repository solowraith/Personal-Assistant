package com.example.personalassistant;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        button.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view)
            {
                Toast.makeText(MainActivity.this, "Click", Toast.LENGTH_SHORT).show();
                return false;
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
    }

    public void microphonePress(View view)
    {
        String fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/recordedAudio.3gp";

        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(fileName);
        try
        {
            recorder.prepare();
        }
        catch (IOException e)
        {
            Log.e("AudioRecordTest", "prepare() failed");
        }
    }
}