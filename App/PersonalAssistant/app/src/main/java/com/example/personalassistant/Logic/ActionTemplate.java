package com.example.personalassistant.Logic;

public class ActionTemplate
{
    private final String[] CHOSEN_ACTION = new String[4];
    // Array layout: [Action, time, date, data]

    public void setAction(String action, String[] brokenUserCommands)
    {
        switch (action)
        {
            case "alarm":
                CHOSEN_ACTION[0] = brokenUserCommands[2];
                CHOSEN_ACTION[1] = brokenUserCommands[4] + ":" + brokenUserCommands[5];
                CHOSEN_ACTION[2] = null;
                CHOSEN_ACTION[3] = brokenUserCommands[6];
                break;

            case "timer":
                CHOSEN_ACTION[0] = brokenUserCommands[2];
                CHOSEN_ACTION[1] = brokenUserCommands[4];
                CHOSEN_ACTION[2] = null;
                CHOSEN_ACTION[3] = brokenUserCommands[5];
                break;

            case "reminder":
                CHOSEN_ACTION[0] = brokenUserCommands[2];
                CHOSEN_ACTION[1] = "";
                CHOSEN_ACTION[2] = "";
                CHOSEN_ACTION[3] = "";
                break;

            case "increase":
                CHOSEN_ACTION[0] = brokenUserCommands[0];
                CHOSEN_ACTION[1] = null;
                CHOSEN_ACTION[2] = null;
                CHOSEN_ACTION[3] = "5";
                break;

            case "decrease":
                CHOSEN_ACTION[0] = brokenUserCommands[0];
                CHOSEN_ACTION[1] = "";
                CHOSEN_ACTION[2] = "";
                CHOSEN_ACTION[3] = "-5";
                break;
        }

    }

    public String[] getAction()
    {
        return CHOSEN_ACTION;
    }

}
