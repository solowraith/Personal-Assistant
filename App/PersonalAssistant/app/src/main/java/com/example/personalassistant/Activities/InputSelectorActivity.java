package com.example.personalassistant.Activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.personalassistant.R;

public class InputSelectorActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_selector);
    }

    private void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button3:
                break;

            case R.id.button4:
                break;

            case R.id.button5:
                break;

            case R.id.button6:
                break;

            case R.id.button7:
                break;

            case R.id.button8:
                break;

            case R.id.button9:
                break;

            case R.id.button10:
                break;
        }
        /*
        //Gets the text of buttons pressed
        Button buttonPressed = (Button) findViewById(v.getId());
        String buttonText = buttonPressed.getText().toString();
        if(buttonText.equals(R.string.alarm))
        {

        }

        if(buttonText.equals(R.string.timer))
        {

        }

        if(buttonText.equals(R.string.reminder))
        {

        }

        if(buttonText.equals(R.string.dec_brightness))
        {

        }

        if(buttonText.equals(R.string.inc_brightness))
        {

        }

        if(buttonText.equals(R.string.open_app))
        {

        }
         */
    }

    public void toMain(View view)
    {
        onBackPressed();
    }
}
