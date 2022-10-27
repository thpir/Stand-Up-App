package com.thpir.standup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    // We create member constants for the notification ID and the notification channel ID
    private static final int NOTIFICATION_ID = 0;
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";

    @Override
    public void onReceive(Context context, Intent intent) {

        // Create an instance of the NotificationHelper class
        NotificationHelper notificationHelper = new NotificationHelper();

        // Call the createNotificationManager method from the NotificationHelper class
        notificationHelper.createNotificationManager(context);
        
        // Call the method to deliver the notification
        notificationHelper.deliverNotification(context, NOTIFICATION_ID, PRIMARY_CHANNEL_ID);

        // Create an instance of the AlarmManager class
        com.thpir.standup.AlarmManager alarmManager = new com.thpir.standup.AlarmManager();

        // We create member constants for the notification ID
        final int NOTIFICATION_ID = 0;

        // Create the alarm
        alarmManager.createAlarm(context, NOTIFICATION_ID);

        // Set the alarm
        alarmManager.setAlarm(context);
    }


}