package com.example.personalassistant.Logic;

import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.widget.Toast;
import android.provider.Settings;
import static android.widget.Toast.makeText;

public class TextParser
{
    ActionTemplate template;
    Context context;
    Intent intent;

    public TextParser(String userCommand, Context context)
    {
        this.context = context;

        if(userCommand != null)
        {
            this.template = classifyString(userCommand);

            if(template.getAction().action != null)
                this.intent = intentBuilder(template);
            else
                makeText(context, "Couldn't resolve Template", Toast.LENGTH_SHORT).show();
        }
        else
            makeText(context, "Couldn't resolve input", Toast.LENGTH_SHORT).show();
    }

    private ActionTemplate classifyString(String userCommand)
    {
        String[] brokenString = breakString(userCommand);
        String decipheredAction;

        switch(brokenString[0])
        {
            case "increase":
            case "decrease":
                decipheredAction = brokenString[0];
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
                        makeText(context, "Couldn't parse input", Toast.LENGTH_LONG).show();
                        decipheredAction = "N/A";
                        break;
                }
        }

        ActionTemplate template = new ActionTemplate();

        switch(template.setAction(decipheredAction, brokenString))
        {
            case -1:
                makeText(context, "Alarm hour out of bounds", Toast.LENGTH_LONG).show();
                return null;
            case -2:
                makeText(context, "Alarm minute out of bounds", Toast.LENGTH_LONG).show();
                return null;
            case -3:
                makeText(context, "Reminder day out of bounds", Toast.LENGTH_LONG).show();
                return null;
            case -4:
                makeText(context, "Reminder month out of bounds", Toast.LENGTH_LONG).show();
                return null;
            case -5:
                makeText(context, "Could not parse date", Toast.LENGTH_LONG).show();
                return null;
            case -10:
                makeText(context, "Parsing error in Action Log", Toast.LENGTH_LONG).show();
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

                    default:
                        break;
                }

                break;

            case "reminder":
                intent = new Intent(Intent.ACTION_INSERT);
                intent.setData(CalendarContract.Events.CONTENT_URI);
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, actionable.getAction().date.getTime());
                intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, actionable.getAction().date.getTime() + 86400001);
                intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
                break;

            case "increase":
                //Settings.System.putInt(context, Settings.System.SCREEN_BRIGHTNESS, Integer.parseInt(Settings.System.SCREEN_BRIGHTNESS) * (Integer.parseInt(actionable.getAction().data)/100));
                break;

            case "decrease":
                break;

            default:
                makeText(context, "Couldn't build intent, input not recognized", Toast.LENGTH_SHORT).show();
                break;
        }

        return intent;
    }

    public Intent getIntent()
    {
        return intent;
    }
}
