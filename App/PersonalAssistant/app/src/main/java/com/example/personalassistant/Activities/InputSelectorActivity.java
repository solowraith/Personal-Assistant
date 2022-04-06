package com.example.personalassistant.Activities;

import static android.widget.Toast.makeText;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.text.InputType;
import android.text.format.Time;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.personalassistant.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

public class InputSelectorActivity extends AppCompatActivity {
    private final int[] contextList = {R.id.setReminder, R.id.setAlarm, R.id.setTimer,
            R.id.setActPhrase};

    private final Button[] buttonList = new Button[contextList.length];
    private final float clickedPos = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_selector);

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
                        String userInput = input.getText().toString();

                        String state = Environment.getExternalStorageState();
                        if (!Environment.MEDIA_MOUNTED.equals(state)) {
                            //If it isn't mounted - we can't write into it.
                            return;
                        }

                        File activationPhrase = new File(InputSelectorActivity.this.getExternalFilesDir(null), "activationPhrase.txt");
                        FileOutputStream outputStream;

                        try {
                            if (activationPhrase.createNewFile() || activationPhrase.isFile()) {
                                outputStream = new FileOutputStream(activationPhrase, false);
                                outputStream.write(userInput.toLowerCase().getBytes());
                                outputStream.write("\n".getBytes());
                                outputStream.flush();
                                outputStream.close();
                            } else
                                makeText(InputSelectorActivity.this, "Couldn't write to activationPhrase.txt", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
}
