package com.example.personalassistant.Logic;


import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;

public class ActionTemplate {
    private final ActionContainer CONTAINER = new ActionContainer();
    // Array layout: [Action, time, date, data]

    /*
    Initializes the Action object, using the classified action from TextParser.classifyString
    to determine the proper placement for the required information within the user's command
     */
    public int setAction(String action, String[] brokenUserCommands) {
        switch (action) {
            case "alarm":
                if (CONTAINER.stringToNum(brokenUserCommands[4]) > 12 || CONTAINER.stringToNum(brokenUserCommands[4]) < 1) {
                    Log.e("personalAssistant.ActionTemplate", "Alarm hour out of bounds");
                    Thread.dumpStack();
                    return -1;
                } else if (CONTAINER.stringToNum(brokenUserCommands[5]) > 60 || CONTAINER.stringToNum(brokenUserCommands[5]) < 0) {
                    Log.e("personalAssistant.ActionTemplate", "Alarm minute out of bounds");
                    Thread.dumpStack();
                    return -2;
                }

                //Filling the Action object's fields directly from the user's commands
                CONTAINER.action = brokenUserCommands[2];
                CONTAINER.time = CONTAINER.stringToNum(brokenUserCommands[4]) + ":" + CONTAINER.stringToNum(brokenUserCommands[5]);
                CONTAINER.colonIndex = CONTAINER.time.indexOf(':');
                CONTAINER.data = brokenUserCommands[6];
                break;

            case "timer":
                //Filling the Action object's fields directly from the user's commands
                CONTAINER.action = brokenUserCommands[2];
                CONTAINER.time = "" + CONTAINER.stringToNum(brokenUserCommands[4]);
                CONTAINER.data = brokenUserCommands[5];
                break;

            case "reminder":
                if (CONTAINER.dateToNum(brokenUserCommands[5]) + CONTAINER.dateToNum(brokenUserCommands[6]) > 31 ||
                        CONTAINER.dateToNum(brokenUserCommands[5]) + CONTAINER.dateToNum(brokenUserCommands[6]) < 1) {
                    Log.e("personalAssistant.ActionTemplate", "Reminder day out of bounds");
                    Thread.dumpStack();
                    return -3;
                } else if (CONTAINER.dateToNum(brokenUserCommands[4]) > 12 ||
                        CONTAINER.dateToNum(brokenUserCommands[4]) < 1) {
                    Log.e("personalAssistant.ActionTemplate", "Reminder month out of bounds");
                    Thread.dumpStack();
                    return -4;
                }

                //Using Java's date formater, transforms the user's commands into a format that will
                //later be able to be understood by intentBuilder() as a proper date form.
                String pattern = "dd-MM-yyyy";
                SimpleDateFormat format = new SimpleDateFormat(pattern);
                String stringDate = (CONTAINER.dateToNum(brokenUserCommands[5]) + CONTAINER.dateToNum(brokenUserCommands[6])) + "-" + CONTAINER.dateToNum(brokenUserCommands[4]) + "-" + LocalDate.now().getYear();
                Date date;
                try {
                    date = format.parse(stringDate);
                } catch (ParseException e) {
                    Log.e("personalAssistant.ActionTemplate", "Could not parse date information");
                    e.printStackTrace();
                    return -5;
                }

                //Filling the Action object's fields directly from the user's commands
                CONTAINER.action = brokenUserCommands[2];
                CONTAINER.time = "";
                CONTAINER.date = date;
                CONTAINER.data = "";
                break;

            case "brightness":
                //Filling the Action object's fields directly from the user's commands
                CONTAINER.action = brokenUserCommands[1];
                //Calculates the new brightness for the screen in the case where there may be two
                //separate words for one number (ex. one hundred, twenty five)
                int newBrightness = CONTAINER.stringToNum(brokenUserCommands[3]) +
                        CONTAINER.stringToNum(brokenUserCommands[4]);
                CONTAINER.data = "" + newBrightness;
                break;

            case "FFSearch":
                //In the case of Freeform searching, no template is required as it needs only be
                //sent directly to the user's browser as is with no pre-processing applied
                CONTAINER.action = "FFSearch";
                break;

            default:
                Log.e("personalAssistant.ActionTemplate", "Could not build template");
                Thread.dumpStack();
                return -10;
        }
        return 0;
    }

    public ActionContainer getActionContainer() {
        return CONTAINER;
    }
}

class ActionContainer {
    String action = "";
    String time = "";
    int colonIndex;
    Date date;
    String data = "";

    /*
    Converts user's input of numbers as words into actionable integer values by utalizing hashmaps
    for quick lookup functionality
     */
    protected int stringToNum(String string) {
        if (string == null)
            return 0;

        int num;
        HashMap<String, Integer> timerMap = new HashMap<>();

        String[] timerValues = {"zero", "one", "two", "three", "four", "five", "six", "seven",
                "eight", "nine", "ten", "twenty", "thirty", "half", "forty", "fifty", "sixty", "oh", "o'clock",
                "seventy", "eighty", "ninety", "hundred"};


        for (int i = 0; i < 11; i++)
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

        if (timerMap.get(string) == null)
            return 0;
        else
            num = timerMap.get(string);

        return num;
    }

    /*
    Converts user's input of months/days as words into actionable integer values by utalizing hashmaps
    for quick lookup functionality
     */
    protected int dateToNum(String string) {
        if (string == null)
            return 0;

        int result;
        HashMap<String, Integer> dayMap = new HashMap<>();
        HashMap<String, Integer> monthMap = new HashMap<>();

        String[] monthValues = {"january", "february", "march", "april", "may", "june", "july",
                "august", "september", "october", "november", "december"};

        String[] dayValues = {"first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth",
                "ninth", "tenth", "eleventh", "twelfth", "thirteenth", "fourteenth", "fifteenth", "sixteenth",
                "seventeenth", "eightteenth", "nineteenth", "twentieth", "twenty", "thirtieth", "thirty"};

        for (int i = 1; i < monthValues.length; i++)
            monthMap.put(monthValues[i - 1], i);

        for (int i = 1; i < 20; i++)
            dayMap.put(dayValues[i - 1], i);

        dayMap.put(dayValues[20], 20);
        dayMap.put(dayValues[21], 30);
        dayMap.put(dayValues[22], 30);

        if (dayMap.get(string) == null)
            result = monthMap.get(string);
        else
            result = dayMap.get(string);

        return result;
    }

    public String getAction() {
        return action;
    }

    public Date getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        String actionData = "\naction: " + action;
        String timeData = "\ntime: " + time;
        String dateData = "\ndate: " + date;
        String dataData = "\ndata: " + data;
        System.out.printf("%naction:%s%ntime:%s%ndate:%s%ndata:%s%n", action, time, date, data);
        return actionData + timeData + dateData + dataData;
    }
}
