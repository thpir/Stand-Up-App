package com.thpir.standup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class AlarmBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            // Initialize the shared preferences
            String mSharedPreferencesFile = "com.thpir.standup";
            SharedPreferences mSharedPreferences = context.getSharedPreferences(mSharedPreferencesFile,
                    Context.MODE_PRIVATE);
            // Private boolean that show whether the alarm is on or off
            boolean mAlarmUp = mSharedPreferences.getBoolean("ALARM_UP", false);

            // Create an instance of the AlarmManager class
            com.thpir.standup.AlarmManager alarmManager = new com.thpir.standup.AlarmManager();

            if (mAlarmUp) {
                // We create member constants for the notification ID
                final int NOTIFICATION_ID = 0;

                // Create the alarm
                alarmManager.createAlarm(context, NOTIFICATION_ID);

                // Set the alarm
                alarmManager.setAlarm(context);
            }
        }
    }
}