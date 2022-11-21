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
            int mInterval = mSharedPreferences.getInt("INTERVAL", 3600000);

            if (mAlarmUp) {

                // Create an instance of the NotificationHelper class
                NotificationHelper notificationHelper = new NotificationHelper();

                // Create a notification
                notificationHelper.createNotificationManager(context);

                // Create an instance of the AlarmManager class
                AlarmHelper alarmHelper = new AlarmHelper();

                // Create the alarm
                alarmHelper.createAlarm(context);

                // Set the alarm
                if (!alarmHelper.canScheduleAlarms()) {
                    alarmHelper.setAlarm(context, mInterval);
                }

                // We call createNotificationChannel() at the end of the onReceive() method
                notificationHelper.createNotificationChannel(context);
            }
        }
    }
}