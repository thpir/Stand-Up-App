package com.thpir.standup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationHelper {
    // We create a member variable called mNotificationManager of the type NotificationManager
    private var mNotificationManager: NotificationManager? = null
    fun createNotificationManager(context: Context) {

        // We initialize mNotificationManager using getSystemService()
        mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun cancelNotification() {

        // We call cancelAll() on the NotificationManager if the toggle is turned off
        // to remove the notification
        mNotificationManager!!.cancelAll()
    }

    fun deliverNotification(context: Context) {

        // We create an Intent that we will use for the notification content intent
        val contentIntent = Intent(context, MainActivity::class.java)

        // After the definition of contentIntent,
        // we create a PendingIntent from the content intent.
        // We use the getActivity() method,
        // passing in the notification ID and using the FLAG_UPDATE_CURRENT flag
        val contentPendingIntent =
            PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE)

        // We use the NotificationCompat.Builder to build a notification using the notification
        // icon and content intent
        val builder = NotificationCompat.Builder(context, "CHANNEL_0")
            .setSmallIcon(R.drawable.ic_stand_up)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text))
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        // At the end of the deliverNotification() method,
        // We use the NotificationManager to deliver the notification
        mNotificationManager!!.notify(0, builder.build())
    }

    /**
     * Creates a Notification channel, for OREO and higher.
     */
    fun createNotificationChannel(context: Context) {

        // Create a notification manager object.
        mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            // Create the NotificationChannel with all the parameters.
            val notificationChannel = NotificationChannel(
                "CHANNEL_0",
                "Stand up notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Notifies every 15 minutes to stand up and walk"
            mNotificationManager!!.createNotificationChannel(notificationChannel)
        }
    }
}