package com.thpir.standup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Initialize the shared preferences
        String mSharedPreferencesFile = "com.thpir.standup";
        SharedPreferences mSharedPreferences = context.getSharedPreferences(mSharedPreferencesFile,
                Context.MODE_PRIVATE);
        // Private boolean that show whether the alarm is on or off + start en end time
        int mInterval = mSharedPreferences.getInt("INTERVAL", 3600000);
        int mStartTimeHour = mSharedPreferences.getInt("START_HOURS", 9);
        int mStartTimeMinutes = mSharedPreferences.getInt("START_MINUTES", 0);
        int mStopTimeHour = mSharedPreferences.getInt("STOP_HOURS", 18);
        int mStopTimeMinutes = mSharedPreferences.getInt("STOP_MINUTES", 0);
        boolean mWeekDaysOnly = mSharedPreferences.getBoolean("WEEKDAYS_ONLY", false);

        // Get current time
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        // Create an instance of the NotificationHelper class
        NotificationHelper notificationHelper = new NotificationHelper();

        // Call the createNotificationManager method from the NotificationHelper class
        notificationHelper.createNotificationManager(context);

        if(((hour > mStartTimeHour) || (hour == mStartTimeHour && minute >= mStartTimeMinutes)) &&
                (hour < mStopTimeHour) || (hour == mStopTimeHour && minute <= mStopTimeMinutes)) {

            if(mWeekDaysOnly) {
                if(day != Calendar.SATURDAY && day != Calendar.SUNDAY) {
                    // Call the method to deliver the notification
                    notificationHelper.deliverNotification(context);
                }
            } else {
                // Call the method to deliver the notification
                notificationHelper.deliverNotification(context);
            }

        }

        // Create an instance of the AlarmManager class
        com.thpir.standup.AlarmManager alarmManager = new com.thpir.standup.AlarmManager();

        // Create the alarm
        alarmManager.createAlarm(context);

        // Set the alarm
        alarmManager.setAlarm(context, mInterval);
    }


}