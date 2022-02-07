package com.example.personalassistant.Logic;

import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;
import android.widget.Toast;
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
        template.setAction(decipheredAction, brokenString);

        return template;
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
                brokenString[count++] = temp;
                temp = "";
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
                intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);

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
                break;

            case "increase":
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
