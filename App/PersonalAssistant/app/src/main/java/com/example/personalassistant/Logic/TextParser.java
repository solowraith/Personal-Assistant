package com.example.personalassistant.Logic;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.widget.Toast;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

import static android.widget.Toast.makeText;

import java.util.Date;

public class TextParser extends AppCompatActivity
{
    private ActionTemplate template;
    private final Context REFERENCE;
    private Intent intent;

    public TextParser(String userCommand, Context context, boolean FFsearch)
    {
        REFERENCE = context;

        //Gate for FFsearch as freeform searching is immediately handled
        if(userCommand != null && !FFsearch)
        {
            //First initializes the action template
            template = classifyString(userCommand);

            //Then creates the intent using the action template
            if(template != null && template.getAction().action != null)
                intent = intentBuilder(template);
            else
                makeText(REFERENCE, "Couldn't resolve Template", Toast.LENGTH_SHORT).show();
        }
        else if(userCommand != null && FFsearch)
        {
            template = new ActionTemplate();
            template.setAction("FFSearch", breakString(userCommand));

            intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, userCommand);
        }
        else makeText(REFERENCE, "Couldn't resolve input", Toast.LENGTH_SHORT).show();
    }

    private ActionTemplate classifyString(String userCommand)
    {
        String[] brokenString = breakString(userCommand);
        String decipheredAction;

        //Due to the user commands being in a structured format, all placements for necessary
        //parts are known in advance and can be referenced directly
        switch(brokenString[1])
        {
            case "brightness":
                decipheredAction = brokenString[1];
                break;

            default:
                switch (brokenString[2])
                {
                    case "alarm":
                    case "timer":
                    case "reminder":
                        decipheredAction = brokenString[2];
                        break;

                    default:
                        makeText(REFERENCE, "Couldn't parse input", Toast.LENGTH_LONG).show();
                        decipheredAction = "N/A";
                        break;
                }
        }

        ActionTemplate template = new ActionTemplate();

        //Handles possible errors for the various inputs from the user
        switch(template.setAction(decipheredAction, brokenString))
        {
            case -1:
                makeText(REFERENCE, "Alarm hour out of bounds", Toast.LENGTH_LONG).show();
                return null;
            case -2:
                makeText(REFERENCE, "Alarm minute out of bounds", Toast.LENGTH_LONG).show();
                return null;
            case -3:
                makeText(REFERENCE, "Reminder day out of bounds", Toast.LENGTH_LONG).show();
                return null;
            case -4:
                makeText(REFERENCE, "Reminder month out of bounds", Toast.LENGTH_LONG).show();
                return null;
            case -5:
                makeText(REFERENCE, "Could not parse date", Toast.LENGTH_LONG).show();
                return null;
            case -10:
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
    private String[] breakString(String input)
    {
        int wordCount = 2;
        for(char character: input.toCharArray())
            if(character == ' ')
                wordCount++;

        String[] brokenString = new String[wordCount];
        String temp = "";
        int count = 0;

        for(char character: input.toCharArray())
        {
            if(character != ' ')
                temp += character;
            else
            {
                if(count < brokenString.length)
                {
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
    private Intent intentBuilder(ActionTemplate actionable)
    {
        Intent intent = null;

        switch(actionable.getAction().action)
        {
            case "alarm":
                intent = new Intent(AlarmClock.ACTION_SET_ALARM);
                intent.putExtra(AlarmClock.EXTRA_MESSAGE, "Personal Assistant Alarm");
                intent.putExtra(AlarmClock.EXTRA_HOUR, Integer.parseInt(actionable.getAction().time.substring(0,actionable.getAction().colonIndex)));
                intent.putExtra(AlarmClock.EXTRA_MINUTES, Integer.parseInt(actionable.getAction().time.substring(actionable.getAction().colonIndex+1)));
                intent.putExtra(AlarmClock.EXTRA_IS_PM, actionable.getAction().data.equals("pm"));
                intent.putExtra(AlarmClock.EXTRA_SKIP_UI, false);
                break;

            case "timer":
                intent = new Intent(AlarmClock.ACTION_SET_TIMER);
                intent.putExtra(AlarmClock.EXTRA_SKIP_UI, false);

                switch(actionable.getAction().data)
                {
                    case"hours":
                        intent.putExtra(AlarmClock.EXTRA_LENGTH, (Integer.parseInt(actionable.getAction().time) * 3600));
                        break;

                    case "minutes":
                        intent.putExtra(AlarmClock.EXTRA_LENGTH, (Integer.parseInt(actionable.getAction().time) * 60));
                        break;

                    case "seconds":
                        intent.putExtra(AlarmClock.EXTRA_LENGTH, Integer.parseInt(actionable.getAction().time));
                        break;
                }
                break;

            case "reminder":
                long addOn = 0;
                int nextDay = 86400001;
                Date current = new Date();

                //If the desired date is for a day/month pair that has already passed, then addOn
                //is set such that the date of the reminder is the following year
                if(actionable.getAction().date.getTime() < current.getTime())
                    addOn = 31556952000L;

                intent = new Intent(Intent.ACTION_INSERT);
                intent.setData(CalendarContract.Events.CONTENT_URI);
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, actionable.getAction().date.getTime() + addOn);
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, actionable.getAction().date.getTime() + nextDay + addOn);
                intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
                break;

            case "brightness":
                //Calculates the new brightness from the user's desired level against the max possible
                final float maxBrightness = 255;
                float newBrightness = maxBrightness * ((float) Integer.parseInt(actionable.getAction().data) / 100f);

                Settings.System.putInt(REFERENCE.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

                Settings.System.putInt(REFERENCE.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                        (int) newBrightness);
                break;

            default:
                makeText(REFERENCE, "Couldn't build intent, input not recognized", Toast.LENGTH_SHORT).show();
                break;
        }

        return intent;
    }

    public Intent getIntent()
    {
        return intent;
    }

    public String getAction()
    {
        return template.getAction().action;
    }
}