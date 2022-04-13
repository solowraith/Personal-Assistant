package com.example.personalassistant.Logic;

import static android.widget.Toast.makeText;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

public class TextParser extends AppCompatActivity {
    private final String LOG_TAG_TP = "TextProcessing";
    private final Context REFERENCE;
    boolean showUITimer = false;
    boolean showUIAlarm = false;
    private ActionTemplate template;
    private Intent intent;

    public TextParser(String userCommand, Context context, boolean FFsearch) {
        REFERENCE = context;

        //Gate for FFsearch as freeform searching is immediately handled
        if (userCommand != null && !FFsearch) {
            //First initializes the action template
            template = classifyString(userCommand);

            //Then creates the intent using the action template
            if (template != null && template.getActionContainer().action != null)
                intent = intentBuilder(template);
            else
                makeText(REFERENCE, "Couldn't resolve Template", Toast.LENGTH_SHORT).show();
        } else if (userCommand != null && FFsearch) {
            template = new ActionTemplate();
            template.setAction("FFSearch", breakString(userCommand));

            intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, userCommand);
        } else {
            Log.e(LOG_TAG_TP, "Couldn't resolve given input, usercommand = null");
            makeText(REFERENCE, "Couldn't resolve input", Toast.LENGTH_SHORT).show();
            template.setAction("error", new String[]{""});
            intent = null;
        }
    }

    private ActionTemplate classifyString(String userCommand) {
        String[] brokenString = breakString(userCommand);
        String decipheredAction;

        if (brokenString.length <= 3 || brokenString == null) //Length=1 only when an error has occured
        {
            ActionTemplate template = new ActionTemplate();
            template.setAction("error", new String[]{""});
            return template;
        }

        //Due to the user commands being in a structured format, all placements for necessary
        //parts are known in advance and can be referenced directly
        switch (brokenString[1]) {
            case "brightness":
                decipheredAction = brokenString[1];
                break;

            default:
                switch (brokenString[2]) {
                    case "alarm":
                    case "timer":
                    case "reminder":
                        decipheredAction = brokenString[2];
                        break;

                    default:
                        Log.e(LOG_TAG_TP, "Couldn't parse given input:\n" + userCommand != null ? userCommand : "");
                        makeText(REFERENCE, "Couldn't parse input", Toast.LENGTH_LONG).show();
                        decipheredAction = "N/A";
                        break;
                }
        }

        ActionTemplate template = new ActionTemplate();
        String methodTag = ".classifyString";

        //Handles possible errors for the various inputs from the user
        switch (template.setAction(decipheredAction, brokenString)) {
            case -1:
                Log.e(LOG_TAG_TP + methodTag, "Alarm hour out of bounds");
                makeText(REFERENCE, "Alarm hour out of bounds", Toast.LENGTH_LONG).show();
                return null;
            case -2:
                Log.e(LOG_TAG_TP + methodTag, "Alarm minute out of bounds");
                makeText(REFERENCE, "Alarm minute out of bounds", Toast.LENGTH_LONG).show();
                return null;
            case -3:
                Log.e(LOG_TAG_TP + methodTag, "Reminder day out of bounds");
                makeText(REFERENCE, "Reminder day out of bounds", Toast.LENGTH_LONG).show();
                return null;
            case -4:
                Log.e(LOG_TAG_TP + methodTag, "Reminder month out of bounds");
                makeText(REFERENCE, "Reminder month out of bounds", Toast.LENGTH_LONG).show();
                return null;
            case -5:
                Log.e(LOG_TAG_TP + methodTag, "Could not parse date");
                makeText(REFERENCE, "Could not parse date", Toast.LENGTH_LONG).show();
                return null;
            case -10:
                Log.e(LOG_TAG_TP + methodTag, "Parsing error in Action Log");
                makeText(REFERENCE, "Parsing error in Action Log", Toast.LENGTH_LONG).show();
                return null;
            default:
                return template;
        }
    }

    /*
    Utility function for splitting a user's command from "something like this" to an
    array of strings such as {"something", "like", "this"}
     */
    private String[] breakString(String input) {
        int wordCount = 2;
        for (char character : input.toCharArray())
            if (character == ' ')
                wordCount++;

        String[] brokenString = new String[wordCount];
        String temp = "";
        int count = 0;

        for (char character : input.toCharArray()) {
            if (character != ' ')
                temp += character;
            else {
                if (count < brokenString.length) {
                    brokenString[count++] = temp;
                    temp = "";
                }
            }
        }
        brokenString[count] = temp;

        return brokenString;
    }

    /*
    Creates the various intents for the different types of commands
    by using the action template for the relevant information from the user's command
     */
    private Intent intentBuilder(ActionTemplate actionable) {
        Intent intent = null;
        String methodTag = ".intentBuilder";

        showUIAlarm = Boolean.parseBoolean(getPropValue("showUIAlarm", false));
        showUITimer = Boolean.parseBoolean(getPropValue("showUITimer", false));


        switch (actionable.getActionContainer().getAction()) {
            case "alarm":
                int alarmHour = Integer.parseInt(actionable.getActionContainer().getTime().substring(0, actionable.getActionContainer().colonIndex));
                int alarmMin = Integer.parseInt(actionable.getActionContainer().getTime().substring(actionable.getActionContainer().colonIndex + 1));
                boolean isPM = actionable.getActionContainer().getData().equals("pm");

                intent = new Intent(AlarmClock.ACTION_SET_ALARM);
                intent.putExtra(AlarmClock.EXTRA_MESSAGE, "Personal Assistant Alarm");
                intent.putExtra(AlarmClock.EXTRA_HOUR, alarmHour);
                intent.putExtra(AlarmClock.EXTRA_MINUTES, alarmMin);
                intent.putExtra(AlarmClock.EXTRA_IS_PM, isPM);
                intent.putExtra(AlarmClock.EXTRA_SKIP_UI, showUIAlarm);
                break;

            case "timer":
                intent = new Intent(AlarmClock.ACTION_SET_TIMER);
                intent.putExtra(AlarmClock.EXTRA_SKIP_UI, showUITimer);
                int timerLengthH = Integer.parseInt(actionable.getActionContainer().getTime()) * 3600;
                int timerLengthM = Integer.parseInt(actionable.getActionContainer().getTime()) * 60;
                int timerLengthS = Integer.parseInt(actionable.getActionContainer().getTime());

                switch (actionable.getActionContainer().getData()) {
                    case "hours":
                        intent.putExtra(AlarmClock.EXTRA_LENGTH, timerLengthH);
                        break;

                    case "minutes":
                        intent.putExtra(AlarmClock.EXTRA_LENGTH, timerLengthM);
                        break;

                    case "seconds":
                        intent.putExtra(AlarmClock.EXTRA_LENGTH, timerLengthS);
                        break;
                }
                break;

            case "reminder":
                long addOn = 0;
                int nextDay = 86400001;
                Date current = new Date();

                //If the desired date is for a day/month pair that has already passed, then addOn
                //is set such that the date of the reminder is the following year
                if (actionable.getActionContainer().getDate().getTime() < current.getTime())
                    addOn = 31556952000L;

                long beginTime = actionable.getActionContainer().getDate().getTime() + addOn;
                long endTime = beginTime + nextDay + addOn;
                intent = new Intent(Intent.ACTION_INSERT);
                intent.setData(CalendarContract.Events.CONTENT_URI);
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime);
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);
                intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
                break;

            case "brightness":
                //Calculates the new brightness from the user's desired level against the max possible
                final float maxBrightness = 255;
                float newBrightness = maxBrightness * ((float) Integer.parseInt(actionable.getActionContainer().getData()) / 100f);

                Settings.System.putInt(REFERENCE.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

                Settings.System.putInt(REFERENCE.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                        (int) newBrightness);
                break;

            default:
                Log.e(LOG_TAG_TP + methodTag, "Couldn't build intent, input not recognized");
                makeText(REFERENCE, "Couldn't build intent, input not recognized", Toast.LENGTH_SHORT).show();
                break;
        }

        return intent;
    }

    public Intent getIntent() {
        return intent;
    }

    public String getAction() {
        return template.getActionContainer().action;
    }

    public ActionTemplate getActionTemplate() {
        return template;
    }

    protected String getPropValue(String desired, boolean loadDefault) {
        InputStream input;
        try {
            Properties properties = new Properties();
            if (loadDefault)
                input = new FileInputStream(REFERENCE.getExternalFilesDir(null) + "/defaultconfig.properties");//getClass().getClassLoader().getResourceAsStream("defaultconfig.properties");
            else
                input = new FileInputStream(REFERENCE.getExternalFilesDir(null) + "/userconfig.properties");//getClass().getClassLoader().getResourceAsStream("defaultconfig.properties");

            if (input != null) {
                properties.load(input);
            } else {
                throw new FileNotFoundException("File not found in classPath");
            }

            String result = properties.getProperty(desired);
            input.close();
            return result;
        } catch (Exception e) {
            Log.e(LOG_TAG_TP, Log.getStackTraceString(e));
        }
        return "";
    }
}