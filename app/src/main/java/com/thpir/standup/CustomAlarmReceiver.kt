package com.thpir.standup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*

class CustomAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        // Initialize the shared preferences
        val mSharedPreferencesFile = "com.thpir.standup"
        val mSharedPreferences = context.getSharedPreferences(
            mSharedPreferencesFile,
            Context.MODE_PRIVATE
        )
        // Private boolean that show whether the alarm is on or off + start en end time
        val mInterval = mSharedPreferences.getInt("INTERVAL", 3600000)
        val mStartTimeHour = mSharedPreferences.getInt("START_HOURS", 9)
        val mStartTimeMinutes = mSharedPreferences.getInt("START_MINUTES", 0)
        val mStopTimeHour = mSharedPreferences.getInt("STOP_HOURS", 18)
        val mStopTimeMinutes = mSharedPreferences.getInt("STOP_MINUTES", 0)
        val mWeekDaysOnly = mSharedPreferences.getBoolean("WEEKDAYS_ONLY", false)

        // Get current time
        val calendar = Calendar.getInstance()
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        val day = calendar[Calendar.DAY_OF_WEEK]

        // Create an instance of the NotificationHelper class
        val notificationHelper = NotificationHelper()

        // Call the createNotificationManager method from the NotificationHelper class
        notificationHelper.createNotificationManager(context)
        if ((hour > mStartTimeHour || hour == mStartTimeHour && minute >= mStartTimeMinutes) &&
            hour < mStopTimeHour || hour == mStopTimeHour && minute <= mStopTimeMinutes
        ) {
            if (mWeekDaysOnly) {
                if (day != Calendar.SATURDAY && day != Calendar.SUNDAY) {
                    // Call the method to deliver the notification
                    notificationHelper.deliverNotification(context)
                }
            } else {
                // Call the method to deliver the notification
                notificationHelper.deliverNotification(context)
            }
        }

        // Create an instance of the AlarmManager class
        val alarmHelper = AlarmHelper()

        // Create the alarm
        alarmHelper.createAlarm(context)

        // Set the alarm
        alarmHelper.setAlarm(context, mInterval)
    }
}