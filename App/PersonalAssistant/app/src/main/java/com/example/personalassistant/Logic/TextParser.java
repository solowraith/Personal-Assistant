package com.example.personalassistant.Logic;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import static android.widget.Toast.makeText;

public class TextParser
{
    public ActionTemplate classifyString(String userCommand, Context context)
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

        return brokenString;
    }

    public Intent intentBuilder()
    {

        return new Intent();
    }
}
