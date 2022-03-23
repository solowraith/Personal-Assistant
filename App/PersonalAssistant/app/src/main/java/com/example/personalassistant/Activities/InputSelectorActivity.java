package com.example.personalassistant.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.example.personalassistant.R;

import java.util.Calendar;

public class InputSelectorActivity extends AppCompatActivity
{
    private int[] contextList = {R.id.setReminder,R.id.setAlarm,R.id.setTimer,
            R.id.setAssName,R.id.setUsrName};

    private Button[] buttonList = new Button[contextList.length];
    private float clickedPos = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_selector);

        //Attaches references to their respective buttons
        for(int i = 0; i < buttonList.length; i++)
            buttonList[i] = findViewById(contextList[i]);

        buttonList[0].setOnClickListener(new View.OnClickListener() { //Setting a reminder
            @Override
            public void onClick(View view)
            {
                for(Button butt: buttonList)
                    if(butt != buttonList[0])
                        butt.setVisibility(View.INVISIBLE);
                buttonList[0].setY(clickedPos);

                showDialog(0);
            }
        });

        buttonList[1].setOnClickListener(new View.OnClickListener() { //Setting an alarm
            @Override
            public void onClick(View view) {
                for(Button butt: buttonList)
                    if(butt != buttonList[1])
                        butt.setVisibility(View.INVISIBLE);
                buttonList[0].setY(clickedPos);

                showDialog(1);
            }
        });

        buttonList[2].setOnClickListener(new View.OnClickListener() { //Setting an Timer
            @Override
            public void onClick(View view) {
                for(Button butt: buttonList)
                    if(butt != buttonList[1])
                        butt.setVisibility(View.INVISIBLE);
                buttonList[0].setY(clickedPos);

                showDialog(2);
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int dialogID)
    {
        Calendar present = Calendar.getInstance(); //Gets current time/date

        switch(dialogID)
        {
            case 0: //Date Dialog
                DatePickerDialog dateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener(){
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth)
                    {
                        //Converts chosen date to millisecond representation
                        Time chosenDate = new Time();
                        chosenDate.set(dayOfMonth, monthOfYear, year);
                        long dtDob = chosenDate.toMillis(true);
                        int nextDay = 86400001;

                        //Builds intent, similar structure to TextParser.intentBuilder()
                        Intent intent = new Intent(Intent.ACTION_INSERT);
                        intent.setData(CalendarContract.Events.CONTENT_URI);
                        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, dtDob);
                        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, dtDob + nextDay);
                        intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
                        startActivity(intent);

                    }
                }, present.get(Calendar.YEAR),present.get(Calendar.MONTH), present.get(Calendar.DAY_OF_MONTH));
                dateDialog.setMessage("Select date for reminder");

                return dateDialog;

            case 1: //Alarm Dialog
                TimePickerDialog alarmDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        //If the chosen time is 12PM or later, must detect and be set for intent
                        boolean isPM= false;
                        if(hour >= 12)
                            isPM = true;

                        //Builds intent, similar structure to TextParser.intentBuilder()
                        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
                        intent.putExtra(AlarmClock.EXTRA_MESSAGE, "Personal Assistant Alarm");
                        intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
                        intent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
                        intent.putExtra(AlarmClock.EXTRA_IS_PM, isPM);
                        intent.putExtra(AlarmClock.EXTRA_SKIP_UI, false);

                        startActivity(intent);
                    }
                }, 0, 0, false);

                alarmDialog.setMessage("Choose a time for the alarm");

                return alarmDialog;

            case 2: //Timer Dialog
                //Uses same type of dialog as the alarm, however utilizes a 24hr formatted clock
                //to allow the user to set a timer for up to 24 hours
                TimePickerDialog timerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {

                        //Builds intent, similar structure to TextParser.intentBuilder()
                        Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER);
                        intent.putExtra(AlarmClock.EXTRA_SKIP_UI, false);
                        intent.putExtra(AlarmClock.EXTRA_LENGTH, hour*3600 + minute*60);

                        startActivity(intent);
                    }
                }, 0, 0, true);

                timerDialog.setMessage("Choose a length for the timer");

                return timerDialog;

            //TODO: Do something with assistant/user names
            case 3: //Assistant Name Dialog
            case 4: //User Name Dialog

        }

        return null;
    }

    public void toMain(View view)
    {
        //Swaps functionality of previous button, if buttons are hidden then returns the user to
        //the unhidden state of the activity, otherwise returns the user to the main activity screen
        boolean notVisible = false;
        for(Button butt : buttonList)
            if(butt.getVisibility() == View.INVISIBLE)
                notVisible = true;

        Intent intent;
        if(notVisible)
            intent = new Intent(InputSelectorActivity.this, InputSelectorActivity.class);
        else
            intent = new Intent(InputSelectorActivity.this, MainActivity.class);

        startActivity(intent);
    }
}
