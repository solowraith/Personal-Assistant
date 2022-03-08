package com.example.personalassistant.Logic;

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

    public TextParser(String userCommand, Context context)
    {
        REFERENCE = context;

        if(userCommand != null)
        {
            this.template = classifyString(userCommand);

            if(template.getAction().action != null)
                this.intent = intentBuilder(template);
            else
                makeText(REFERENCE, "Couldn't resolve Template", Toast.LENGTH_SHORT).show();
        }
        else
            makeText(REFERENCE, "Couldn't resolve input", Toast.LENGTH_SHORT).show();
    }

    private ActionTemplate classifyString(String userCommand)
    {
        String[] brokenString = breakString(userCommand);
        String decipheredAction;

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

    private String[] breakString(String input)
    {
        String[] brokenString = new String[10];
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

                if(actionable.getAction().date.getTime() < current.getTime())
                    addOn = 31556952000L;

                intent = new Intent(Intent.ACTION_INSERT);
                intent.setData(CalendarContract.Events.CONTENT_URI);
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, actionable.getAction().date.getTime() + addOn);
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, actionable.getAction().date.getTime() + nextDay + addOn);
                intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
                break;

            case "brightness":
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