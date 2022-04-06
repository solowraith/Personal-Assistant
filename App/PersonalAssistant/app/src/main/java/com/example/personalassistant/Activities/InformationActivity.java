package com.example.personalassistant.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.personalassistant.R;

public class InformationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
    }

    public void toMain(View view) {
        startActivity(new Intent(InformationActivity.this, MainActivity.class));
        //Returns to mainActivity
    }
}
