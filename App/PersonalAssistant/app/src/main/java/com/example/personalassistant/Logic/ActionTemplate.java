package com.example.personalassistant.Logic;


import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ActionTemplate
{
    private final Action ACTION = new Action();
    // Array layout: [Action, time, date, data]

    public int setAction(String action, String[] brokenUserCommands)
    {
        switch (action)
        {
            case "alarm":
                if(ACTION.stringToNum(brokenUserCommands[4]) > 12 || ACTION.stringToNum(brokenUserCommands[4]) < 1)
                {
                    Log.e("personalAssistant.ActionTemplate", "Alarm hour out of bounds");
                    Thread.dumpStack();
                    return -1;
                }
                else if(ACTION.stringToNum(brokenUserCommands[5]) > 60 || ACTION.stringToNum(brokenUserCommands[5]) < 0)
                {
                    Log.e("personalAssistant.ActionTemplate", "Alarm minute out of bounds");
                    Thread.dumpStack();
                    return -2;
                }

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
                if(ACTION.dateToNum(brokenUserCommands[5]) + ACTION.dateToNum(brokenUserCommands[6]) > 31 ||
                        ACTION.dateToNum(brokenUserCommands[5]) + ACTION.dateToNum(brokenUserCommands[6])  < 1)
                {
                    Log.e("personalAssistant.ActionTemplate", "Reminder day out of bounds");
                    Thread.dumpStack();
                    return -3;
                }

                else if (ACTION.dateToNum(brokenUserCommands[4]) > 12 ||
                        ACTION.dateToNum(brokenUserCommands[4]) < 1)
                {
                    Log.e("personalAssistant.ActionTemplate", "Reminder month out of bounds");
                    Thread.dumpStack();
                    return -4;
                }

                String pattern = "dd-MM-yyyy";
                SimpleDateFormat format = new SimpleDateFormat(pattern);
                String stringDate = (ACTION.dateToNum(brokenUserCommands[5]) + ACTION.dateToNum(brokenUserCommands[6])) + "-" + ACTION.dateToNum(brokenUserCommands[4]) + "-" + LocalDate.now().getYear();
                Date date;
                try
                {
                    date = format.parse(stringDate);
                }
                catch (ParseException e)
                {
                    Log.e("personalAssistant.ActionTemplate", "Could not parse date information");
                    e.printStackTrace();
                    return -5;
                }

                ACTION.action = brokenUserCommands[2];
                ACTION.time =  "";
                ACTION.date = date;
                ACTION.data = "";
                break;

            case "brightness":
                ACTION.action = brokenUserCommands[1];
                int newBrightness = ACTION.stringToNum(brokenUserCommands[3]) +
                        ACTION.stringToNum(brokenUserCommands[4]);
                ACTION.data = "" + newBrightness;
                break;

            default:
                Log.e("personalAssistant.ActionTemplate", "Could not build template");
                Thread.dumpStack();
                return -10;
        }
        return 0;
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
    Date date;
    String data = "";

    protected int stringToNum(String string)
    {
        if(string == null)
            return 0;

        int num;
        HashMap<String, Integer> timerMap = new HashMap<String, Integer>();

        String[] timerValues = {"zero","one","two","three","four","five","six","seven",
                "eight","nine","ten","twenty","thirty","half","forty","fifty","sixty","oh", "o'clock",
                "seventy", "eighty", "ninety", "hundred"};


        for(int i = 0; i < 11; i++)
            timerMap.put(timerValues[i], i);

        timerMap.put(timerValues[11], 20);
        timerMap.put(timerValues[12], 30);
        timerMap.put(timerValues[13], 30);
        timerMap.put(timerValues[14], 40);
        timerMap.put(timerValues[15], 50);
        timerMap.put(timerValues[16], 60);
        timerMap.put(timerValues[17], 0);
        timerMap.put(timerValues[18], 0);
        timerMap.put(timerValues[19], 70);
        timerMap.put(timerValues[20], 80);
        timerMap.put(timerValues[21], 90);
        timerMap.put(timerValues[22], 100);

        System.err.println("String used: " + string);

        if(timerMap.get(string) == null)
            return 0;
        else
        num = timerMap.get(string).intValue();

        return num;
    }

    protected int dateToNum(String string)
    {
        if(string == null)
            return 0;

        int result;
        HashMap<String, Integer> dayMap = new HashMap<String, Integer>();
        HashMap<String, Integer> monthMap = new HashMap<String, Integer>();

        String[] monthValues = {"january", "february", "march", "april", "may", "june", "july",
                "august", "september", "october", "november", "december"};

        String[] dayValues = {"first","second","third","fourth","fifth","sixth","seventh","eighth",
                "ninth","tenth","eleventh","twelfth","thirteenth","fourteenth","fifteenth","sixteenth",
                "seventeenth","eightteenth","nineteenth","twentieth","twenty","thirtieth","thirty"};

        for(int i = 1; i < monthValues.length; i++)
            monthMap.put(monthValues[i-1], i);

        for(int i = 1; i < 20; i++)
            dayMap.put(dayValues[i - 1], i);

        dayMap.put(dayValues[20], 20);
        dayMap.put(dayValues[21], 30);
        dayMap.put(dayValues[22], 30);

        if(dayMap.get(string) == null)
            result = monthMap.get(string).intValue();
        else
            result = dayMap.get(string).intValue();

        return result;
    }

    protected void dumpParams()
    {
        System.out.printf("%naction:%s%ntime:%s%ndate:%s%ndata:%s%n",action,time,date,data);
    }
}
