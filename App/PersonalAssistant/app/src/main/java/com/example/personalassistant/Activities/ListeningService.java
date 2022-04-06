package com.example.personalassistant.Activities;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.personalassistant.Logic.VoiceProcessing;

public class ListeningService extends Service {
    private final int NOTIF_ID = 1;
    private final String NOTIF_CHANNEL_ID = "Personal_Assistant_ID";
    private VoiceProcessing vp;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        VoiceProcessing vp = MainActivity.vp;
        vp.startListening(true);
        return super.onStartCommand(intent, flags, startID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
