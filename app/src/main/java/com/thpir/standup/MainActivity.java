package com.thpir.standup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Private boolean that show whether the alarm is on or off
    private boolean mAlarmUp = false;
    private int mInterval = 3600000;

    // Shared Preferences
    private SharedPreferences mSharedPreferences;
    private final String mSharedPreferencesFile = "com.thpir.standup";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the shared preferences
        mSharedPreferences = getSharedPreferences(mSharedPreferencesFile, MODE_PRIVATE);
        mAlarmUp = mSharedPreferences.getBoolean("ALARM_UP", false);
        mInterval = mSharedPreferences.getInt("INTERVAL", 3600000);

        // Find the ImageButton and Textview by id
        ImageButton alarmToggle = findViewById(R.id.imageButtonAlarm);
        TextView alarmTextView = findViewById(R.id.textViewAlarm);

        // Set the state of the toggle
        if (mAlarmUp) {
            alarmToggle.setBackgroundResource(R.drawable.round_button_blue);
            alarmToggle.setImageResource(R.drawable.ic_notifications_active);
            alarmTextView.setText(R.string.alarm_on_text);
        } else {
            alarmToggle.setBackgroundResource(R.drawable.round_button_grey);
            alarmToggle.setImageResource(R.drawable.ic_notifications_off);
            alarmTextView.setText(R.string.alarm_off_text);
        }

        // Create an instance of the NotificationHelper class
        NotificationHelper notificationHelper = new NotificationHelper();

        // Create a notification
        notificationHelper.createNotificationManager(MainActivity.this);

        // Create an instance of the AlarmManager class
        com.thpir.standup.AlarmManager alarmManager = new com.thpir.standup.AlarmManager();

        // Create an alarm
        alarmManager.createAlarm(MainActivity.this);

        // Call setOnCheckedChangeListener() on the ToggleButton instance.
        alarmToggle.setOnClickListener(v -> {
            String toastMessage;
            if (!mAlarmUp) {

                // Set the alarm
                alarmManager.setAlarm(MainActivity.this, mInterval);

                // Set the toast message for the "on" case.
                toastMessage = getString(R.string.message_alarm_on);

                // Change the button and textview appearance
                alarmToggle.setBackgroundResource(R.drawable.round_button_blue);
                alarmToggle.setImageResource(R.drawable.ic_notifications_active);
                alarmTextView.setText(R.string.alarm_on_text);

                // Save the toggleButton state to the private boolean mAlarmUp
                mAlarmUp = true;
            } else {

                // Cancel the alarm
                alarmManager.cancelAlarm(MainActivity.this);

                // We call cancelAll() on the NotificationManager if the toggle is turned off
                // to remove the notification
                notificationHelper.cancelNotification();

                // Set the toast message for the "off" case.
                toastMessage = getString(R.string.message_alarm_off);

                // Change the button and textview appearance
                alarmToggle.setBackgroundResource(R.drawable.round_button_grey);
                alarmToggle.setImageResource(R.drawable.ic_notifications_off);
                alarmTextView.setText(R.string.alarm_off_text);

                // Save the toggleButton state to the private boolean mAlarmUp
                mAlarmUp = false;
            }

            // Show a toast to say the alarm is turned on or off.
            Toast.makeText(MainActivity.this, toastMessage,Toast.LENGTH_SHORT)
                    .show();

            // Save the new state to shared preferences
            savedSharedPreferences();
        });

        // We call createNotificationChannel() at the end of the onCreate() method
        notificationHelper.createNotificationChannel(MainActivity.this);

    }

    private void savedSharedPreferences() {

        // get and editor for the SharePreferences object
        mSharedPreferences = getSharedPreferences(mSharedPreferencesFile, MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = mSharedPreferences.edit();
        sharedPreferencesEditor.putBoolean("ALARM_UP", mAlarmUp);
        sharedPreferencesEditor.apply();

    }
}