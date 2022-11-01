package com.thpir.standup;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    // We create a member variable called mNotificationManager of the type NotificationManager
    private NotificationManager mNotificationManager;

    public void createNotificationManager(Context context) {

        // We initialize mNotificationManager using getSystemService()
        mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

    }

    public void cancelNotification() {

        // We call cancelAll() on the NotificationManager if the toggle is turned off
        // to remove the notification
        mNotificationManager.cancelAll();

    }

    public void deliverNotification(Context context) {

        // We create an Intent that we will use for the notification content intent
        Intent contentIntent = new Intent(context, MainActivity.class);

        // After the definition of contentIntent,
        // we create a PendingIntent from the content intent.
        // We use the getActivity() method,
        // passing in the notification ID and using the FLAG_UPDATE_CURRENT flag
        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (context, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE);

        // We use the NotificationCompat.Builder to build a notification using the notification
        // icon and content intent
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CHANNEL_0")
                .setSmallIcon(R.drawable.ic_stand_up)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_text))
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        // At the end of the deliverNotification() method,
        // We use the NotificationManager to deliver the notification
        mNotificationManager.notify(0, builder.build());

    }

    /**
     * Creates a Notification channel, for OREO and higher.
     */
    public void createNotificationChannel(Context context) {

        // Create a notification manager object.
        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {

            // Create the NotificationChannel with all the parameters.
            NotificationChannel notificationChannel = new NotificationChannel
                    ("CHANNEL_0",
                            "Stand up notification",
                            NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription
                    ("Notifies every 15 minutes to stand up and walk");
            mNotificationManager.createNotificationChannel(notificationChannel);

        }
    }
}
