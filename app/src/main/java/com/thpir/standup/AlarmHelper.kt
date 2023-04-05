package com.thpir.standup

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock

class AlarmHelper {
    private var alarmManager: AlarmManager? = null
    private var notifyPendingIntent: PendingIntent? = null
    fun canScheduleAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager != null) {
            !alarmManager!!.canScheduleExactAlarms()
        } else {
            false
        }
    }

    fun createAlarm(context: Context) {

        // We create an Intent called notifyIntent. We pass in the context and AlarmReceiver class.
        val notifyIntent = Intent(context, CustomAlarmReceiver::class.java)

        // Now create the notify PendingIntent. Use the context,
        // the NOTIFICATION_ID variable, the new notify intent, and the FLAG_UPDATE_CURRENT flag.
        notifyPendingIntent =
            PendingIntent.getBroadcast(context, 0, notifyIntent, PendingIntent.FLAG_IMMUTABLE)

        // We initialize the alarmManager in onCreate() by calling getSystemService()
        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    fun setAlarm(context: Context, interval: Int) {

        // You use the setInexactRepeating() alarm because it is more
        // resource-efficient to use inexact timing,
        // which lets the system bundle alarms from different apps together
        val triggerTime = (SystemClock.elapsedRealtime()
                + interval)
        if (alarmManager != null) {
            // If the Toggle is turned on, set the repeating alarm with a 15 minute interval.
            alarmManager!!.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerTime, notifyPendingIntent
            )
        }

        // From developer.android.com: Notice that in the manifest, the boot receiver
        // is set to android:enabled="false". This means that the receiver will not be
        // called unless the application explicitly enables it. This prevents the boot
        // receiver from being called unnecessarily. You can enable a receiver (for
        // example, if the user sets an alarm) as follows:
        val receiverMonthlyAlarm = ComponentName(context, AlarmBootReceiver::class.java)
        val pmMonthlyAlarm = context.packageManager
        pmMonthlyAlarm.setComponentEnabledSetting(
            receiverMonthlyAlarm,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun cancelAlarm(context: Context) {
        if (alarmManager != null) {
            alarmManager!!.cancel(notifyPendingIntent)
        }

        // From developer.android.com: Once you enable the receiver this way, it will
        // stay enabled, even if the user reboots the device. In other words,
        // programmatically enabling the receiver overrides the manifest setting, even
        // across reboots. The receiver will stay enabled until your app disables it.
        // You can disable a receiver (for example, if the user cancels an alarm) as
        // follows:
        val receiverMonthlyAlarm = ComponentName(context, AlarmBootReceiver::class.java)
        val pmMonthlyAlarm = context.packageManager
        pmMonthlyAlarm.setComponentEnabledSetting(
            receiverMonthlyAlarm,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}