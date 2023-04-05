package com.thpir.standup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {

            // Initialize the shared preferences
            val mSharedPreferencesFile = "com.thpir.standup"
            val mSharedPreferences = context.getSharedPreferences(
                mSharedPreferencesFile,
                Context.MODE_PRIVATE
            )
            // Private boolean that show whether the alarm is on or off
            val mAlarmUp = mSharedPreferences.getBoolean("ALARM_UP", false)
            val mInterval = mSharedPreferences.getInt("INTERVAL", 3600000)
            if (mAlarmUp) {

                // Create an instance of the NotificationHelper class
                val notificationHelper = NotificationHelper()

                // Create a notification
                notificationHelper.createNotificationManager(context)

                // Create an instance of the AlarmManager class
                val alarmHelper = AlarmHelper()

                // Create the alarm
                alarmHelper.createAlarm(context)

                // Set the alarm
                if (!alarmHelper.canScheduleAlarms()) {
                    alarmHelper.setAlarm(context, mInterval)
                }

                // We call createNotificationChannel() at the end of the onReceive() method
                notificationHelper.createNotificationChannel(context)
            }
        }
    }
}