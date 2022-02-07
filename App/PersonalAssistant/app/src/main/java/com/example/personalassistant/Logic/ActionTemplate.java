package com.example.personalassistant.Logic;


import android.view.WindowManager;

import java.util.HashMap;

public class ActionTemplate
{
    private final Action ACTION = new Action();
    // Array layout: [Action, time, date, data]

    public void setAction(String action, String[] brokenUserCommands)
    {
        switch (action)
        {
            case "alarm":
                ACTION.action = brokenUserCommands[2];
                ACTION.time = ACTION.stringToNum(brokenUserCommands[4]) + ":" + ACTION.stringToNum(brokenUserCommands[5]);
                ACTION.colonIndex = ACTION.time.indexOf(':');
                ACTION.data = brokenUserCommands[6];
                break;

            case "timer":
                ACTION.action = brokenUserCommands[2];
                ACTION.time = "" + ACTION.stringToNum(brokenUserCommands[4]);
                ACTION.data = brokenUserCommands[5];
                break;

            case "reminder":
                ACTION.action = brokenUserCommands[2];
                ACTION.time =  "";
                ACTION.date = "";
                ACTION.data = "";
                break;

            case "increase":
                ACTION.action = brokenUserCommands[0];
                ACTION.data = "5";
                break;

            case "decrease":
                ACTION.action = brokenUserCommands[0];
                ACTION.data = "-5";
                break;

            default:
                break;
        }

    }

    public Action getAction()
    {
        return ACTION;
    }
}

class Action
{
    String action = "";
    String time = "";
    int colonIndex;
    String date = "";
    String data = "";

    protected int stringToNum(String string)
    {
        int num;
        HashMap<String, Integer> map = new HashMap<String, Integer>();

        String[] values = {"zero","one","two","three","four","five","six","seven",
                "eight","nine","ten","twenty","thirty","half","forty","fifty","sixty","oh", "o'clock"};

        for(int i = 0; i < 11; i++)
        {
            map.put(values[i], i);
        }

        map.put(values[11], 20);
        map.put(values[12], 30);
        map.put(values[13], 30);
        map.put(values[14], 40);
        map.put(values[15], 50);
        map.put(values[16], 60);
        map.put(values[17], 0);
        map.put(values[18], 0);

        num = map.get(string).intValue();

        return num;
    }

    protected void dumpParams()
    {
        System.out.printf("%naction:%s%ntime:%s%ndate:%s%ndata:%s%n",action,time,date,data);
    }
}
