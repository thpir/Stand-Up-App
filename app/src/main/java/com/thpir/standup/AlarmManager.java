package com.thpir.standup;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;

public class AlarmManager {

    private android.app.AlarmManager alarmManager;
    private PendingIntent notifyPendingIntent;

    public boolean canScheduleAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        } else {
            return true;
        }
    }

    public void createAlarm(Context context) {

        // We create an Intent called notifyIntent. We pass in the context and AlarmReceiver class.
        Intent notifyIntent = new Intent(context, AlarmReceiver.class);

        // Now create the notify PendingIntent. Use the context,
        // the NOTIFICATION_ID variable, the new notify intent, and the FLAG_UPDATE_CURRENT flag.
        notifyPendingIntent = PendingIntent.getBroadcast
                (context, 0, notifyIntent, PendingIntent.FLAG_IMMUTABLE);

        // We initialize the alarmManager in onCreate() by calling getSystemService()
        alarmManager = (android.app.AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

    }

    public void setAlarm(Context context, int interval) {

        // You use the setInexactRepeating() alarm because it is more
        // resource-efficient to use inexact timing,
        // which lets the system bundle alarms from different apps together
        long triggerTime = SystemClock.elapsedRealtime()
                + interval;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                if (alarmManager != null) {
                    // If the Toggle is turned on, set the repeating alarm with a 15 minute interval.
                    alarmManager.setExactAndAllowWhileIdle
                            (android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                    triggerTime, notifyPendingIntent);
                }
            }
        } else {
            if (alarmManager != null) {
                // If the Toggle is turned on, set the repeating alarm with a 15 minute interval.
                alarmManager.setExactAndAllowWhileIdle
                        (android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                triggerTime, notifyPendingIntent);
            }
        }



        // From developer.android.com: Notice that in the manifest, the boot receiver
        // is set to android:enabled="false". This means that the receiver will not be
        // called unless the application explicitly enables it. This prevents the boot
        // receiver from being called unnecessarily. You can enable a receiver (for
        // example, if the user sets an alarm) as follows:
        ComponentName receiverMonthlyAlarm = new ComponentName(context, AlarmBootReceiver.class);
        PackageManager pmMonthlyAlarm = context.getPackageManager();
        pmMonthlyAlarm.setComponentEnabledSetting(receiverMonthlyAlarm,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void cancelAlarm(Context context) {

        if (alarmManager != null) {
            alarmManager.cancel(notifyPendingIntent);
        }

        // From developer.android.com: Once you enable the receiver this way, it will
        // stay enabled, even if the user reboots the device. In other words,
        // programmatically enabling the receiver overrides the manifest setting, even
        // across reboots. The receiver will stay enabled until your app disables it.
        // You can disable a receiver (for example, if the user cancels an alarm) as
        // follows:
        ComponentName receiverMonthlyAlarm = new ComponentName(context, AlarmBootReceiver.class);
        PackageManager pmMonthlyAlarm = context.getPackageManager();
        pmMonthlyAlarm.setComponentEnabledSetting(receiverMonthlyAlarm,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

    }

}
