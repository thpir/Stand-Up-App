package com.thpir.standup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Initialize the shared preferences
        String mSharedPreferencesFile = "com.thpir.standup";
        SharedPreferences mSharedPreferences = context.getSharedPreferences(mSharedPreferencesFile,
                Context.MODE_PRIVATE);
        // Private boolean that show whether the alarm is on or off
        int mInterval = mSharedPreferences.getInt("INTERVAL", 0);


        // Create an instance of the NotificationHelper class
        NotificationHelper notificationHelper = new NotificationHelper();

        // Call the createNotificationManager method from the NotificationHelper class
        notificationHelper.createNotificationManager(context);
        
        // Call the method to deliver the notification
        notificationHelper.deliverNotification(context);

        // Create an instance of the AlarmManager class
        com.thpir.standup.AlarmManager alarmManager = new com.thpir.standup.AlarmManager();

        // Create the alarm
        alarmManager.createAlarm(context);

        // Set the alarm
        alarmManager.setAlarm(context, mInterval);
    }


}