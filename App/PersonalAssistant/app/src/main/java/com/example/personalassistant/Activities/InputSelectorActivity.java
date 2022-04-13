package com.example.personalassistant.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.text.InputType;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.example.personalassistant.R;
import com.google.android.material.chip.Chip;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;

public class InputSelectorActivity extends AppCompatActivity {
    private static final String LOG_TAG_IS = "InputSelector";
    private final int[] contextList = {R.id.setReminder, R.id.setAlarm, R.id.setTimer,
            R.id.setActPhrase, R.id.editConfig};
    private final int[] chipContext = {R.id.showUIAlarm, R.id.showUITimer};
    private final Chip[] chipsList = new Chip[chipContext.length];
    private final Button[] buttonList = new Button[contextList.length];
    private final float clickedPos = 200;
    private boolean UITimer;
    private boolean UIAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_selector);
        UITimer = Boolean.parseBoolean(getPropValue("showUITimer", false));
        UIAlarm = Boolean.parseBoolean(getPropValue("showUIAlarm", false));

        for (int i = 0; i < chipContext.length; i++) {
            chipsList[i] = findViewById(chipContext[i]);
            chipsList[i].setVisibility(Chip.INVISIBLE);
        }

        if (UIAlarm) {
            chipsList[0].setTextColor(getResources().getColor(R.color.white));
            chipsList[0].setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.green)));
        } else {
            chipsList[0].setTextColor(getResources().getColor(R.color.black));
            chipsList[0].setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.red)));
        }

        if (UITimer) {
            chipsList[1].setTextColor(getResources().getColor(R.color.white));
            chipsList[1].setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.green)));
        } else {
            chipsList[1].setTextColor(getResources().getColor(R.color.black));
            chipsList[1].setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.red)));
        }

        //Attaches references to their respective buttons
        for (int i = 0; i < buttonList.length; i++)
            buttonList[i] = findViewById(contextList[i]);

        buttonList[0].setOnClickListener(new View.OnClickListener() { //Setting a reminder
            @Override
            public void onClick(View view) {
                for (Button butt : buttonList)
                    if (butt != buttonList[0])
                        butt.setVisibility(View.INVISIBLE);
                buttonList[0].setY(clickedPos);
                showDialog(0);
            }
        });

        buttonList[1].setOnClickListener(new View.OnClickListener() { //Setting an alarm
            @Override
            public void onClick(View view) {
                for (Button butt : buttonList)
                    if (butt != buttonList[1])
                        butt.setVisibility(View.INVISIBLE);
                buttonList[1].setY(clickedPos);

                showDialog(1);
            }
        });

        buttonList[2].setOnClickListener(new View.OnClickListener() { //Setting an Timer
            @Override
            public void onClick(View view) {
                for (Button butt : buttonList)
                    if (butt != buttonList[2])
                        butt.setVisibility(View.INVISIBLE);
                buttonList[2].setY(clickedPos);

                showDialog(2);
            }
        });

        buttonList[3].setOnClickListener(new View.OnClickListener() { //Setting Activation Phrase
            @Override
            public void onClick(View view) {
                for (Button butt : buttonList)
                    if (butt != buttonList[3])
                        butt.setVisibility(View.INVISIBLE);
                buttonList[3].setY(clickedPos);

                showDialog(3);
            }
        });

        buttonList[4].setOnClickListener(new View.OnClickListener() { //Edit Config for showUI's
            @Override
            public void onClick(View view) {
                for (Button butt : buttonList)
                    if (butt != buttonList[4])
                        butt.setVisibility(View.INVISIBLE);

                for (Chip chip : chipsList)
                    if (chip.getVisibility() == Chip.INVISIBLE)
                        chip.setVisibility(Chip.VISIBLE);

                chipsList[0].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UIAlarm = !UIAlarm;
                        setProperty("showUIAlarm", "" + UIAlarm);
                        if (UIAlarm) {
                            chipsList[0].setTextColor(getResources().getColor(R.color.white));
                            chipsList[0].setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                        } else {
                            chipsList[0].setTextColor(getResources().getColor(R.color.black));
                            chipsList[0].setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                        }
                    }
                });

                chipsList[1].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UITimer = !UITimer;
                        setProperty("showUITimer", "" + UITimer);
                        if (UITimer) {
                            chipsList[1].setTextColor(getResources().getColor(R.color.white));
                            chipsList[1].setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                        } else {
                            chipsList[1].setTextColor(getResources().getColor(R.color.black));
                            chipsList[1].setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                        }
                    }
                });

                buttonList[4].setY(clickedPos);

                showDialog(4);
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int dialogID) {
        Calendar present = Calendar.getInstance(); //Gets current time/date

        switch (dialogID) {
            case 0: //Date Dialog
                DatePickerDialog dateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
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
                }, present.get(Calendar.YEAR), present.get(Calendar.MONTH), present.get(Calendar.DAY_OF_MONTH));
                dateDialog.setMessage("Select date for reminder");

                return dateDialog;

            case 1: //Alarm Dialog
                TimePickerDialog alarmDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        //If the chosen time is 12PM or later, must detect and be set for intent
                        boolean isPM = hour >= 12;

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
                        intent.putExtra(AlarmClock.EXTRA_LENGTH, hour * 3600 + minute * 60);

                        startActivity(intent);
                    }
                }, 0, 0, true);

                timerDialog.setMessage("Choose a length for the timer");

                return timerDialog;

            case 3: //Assistant Name Dialog
                AlertDialog.Builder inputDialog = new AlertDialog.Builder(this);
                inputDialog.setTitle("Input desired activation phrase (Must only use english words)");

                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                inputDialog.setView(input);

                inputDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setProperty("activationPhrase", input.getText().toString());
                    }
                });

                inputDialog.show();

                break;

        }

        return null;
    }

    public void toMain(View view) {
        //Swaps functionality of previous button, if buttons are hidden then returns the user to
        //the unhidden state of the activity, otherwise returns the user to the main activity screen
        boolean notVisible = false;
        for (Button butt : buttonList)
            if (butt.getVisibility() == View.INVISIBLE)
                notVisible = true;

        Intent intent;
        if (notVisible)
            intent = new Intent(InputSelectorActivity.this, InputSelectorActivity.class);
        else
            intent = new Intent(InputSelectorActivity.this, MainActivity.class);

        startActivity(intent);
    }

    private void setProperty(String propName, String val) {
        try {
            FileInputStream in = new FileInputStream(getExternalFilesDir(null) + "/userconfig.properties");
            Properties prop = new Properties();
            prop.load(in);
            prop.setProperty(propName, val);

            FileOutputStream output = new FileOutputStream(getExternalFilesDir(null) + "/userconfig.properties", false);
            prop.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPropValue(String desired, boolean loadDefault) {
        InputStream input;
        try {
            Properties properties = new Properties();
            if (loadDefault)
                input = new FileInputStream(getExternalFilesDir(null) + "/defaultconfig.properties");//getClass().getClassLoader().getResourceAsStream("defaultconfig.properties");
            else
                input = new FileInputStream(getExternalFilesDir(null) + "/userconfig.properties");//getClass().getClassLoader().getResourceAsStream("defaultconfig.properties");

            if (input != null) {
                properties.load(input);
            } else {
                throw new FileNotFoundException("File not found in classPath");
            }

            String result = properties.getProperty(desired);
            input.close();
            return result;
        } catch (Exception e) {
            Log.e(LOG_TAG_IS, Log.getStackTraceString(e));
        }
        return "";
    }
}
