package com.thpir.standup;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    // Private boolean that show whether the alarm is on or off
    private boolean mAlarmUp = false;
    private boolean mWeekdaysOnly = false;
    private int mInterval = 3600000;
    private int mIntervalPosition = 2;
    private int mStartTimeHour;
    private int mStartTimeMinutes;
    private int mStopTimeHour;
    private int mStopTimeMinutes;
    private ImageButton alarmToggle;
    private TextView alarmTextView;
    private TextView startHour;
    private TextView endHour;
    private final String[] mIntervalList = {"15 Minutes", "30 Minutes", "1 Hour", "2 Hours"};

    // Shared Preferences
    private SharedPreferences mSharedPreferences;
    private final String mSharedPreferencesFile = "com.thpir.standup";

    // Create an instance of the AlarmHelper class
    AlarmHelper alarmHelper = new AlarmHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the shared preferences
        mSharedPreferences = getSharedPreferences(mSharedPreferencesFile, MODE_PRIVATE);
        mAlarmUp = mSharedPreferences.getBoolean("ALARM_UP", false);
        mWeekdaysOnly = mSharedPreferences.getBoolean("WEEKDAYS_ONLY", false);
        mInterval = mSharedPreferences.getInt("INTERVAL", 3600000);
        mIntervalPosition = mSharedPreferences.getInt("INTERVAL_POS", 2);
        mStartTimeHour = mSharedPreferences.getInt("START_HOURS", 9);
        mStartTimeMinutes = mSharedPreferences.getInt("START_MINUTES", 0);
        mStopTimeHour = mSharedPreferences.getInt("STOP_HOURS", 18);
        mStopTimeMinutes = mSharedPreferences.getInt("STOP_MINUTES", 0);

        // Initialize the views of the MainActivity
        TextView intervalTextView = findViewById(R.id.textViewInterval);
        startHour = findViewById(R.id.textViewFromTime);
        endHour = findViewById(R.id.textViewUntilTime);
        ImageView leftArrow = findViewById(R.id.imageViewToggleLeft);
        ImageView rightArrow = findViewById(R.id.imageViewToggleRight);
        CheckBox weekdaysOnly = findViewById(R.id.checkBoxWeekdays);
        alarmToggle = findViewById(R.id.imageButtonAlarm);
        alarmTextView = findViewById(R.id.textViewAlarm);

        // Set the saved start and stop time
        setStartStopTime();

        // Set the weekdays only checkbox
        weekdaysOnly.setChecked(mWeekdaysOnly);

        // Set the state of the interval textview
        intervalTextView.setText(mIntervalList[mIntervalPosition]);

        // Create an instance of the NotificationHelper class
        NotificationHelper notificationHelper = new NotificationHelper();

        // Create a notification
        notificationHelper.createNotificationManager(MainActivity.this);

        // Create an alarm
        alarmHelper.createAlarm(MainActivity.this);

        // When we open the app we want to check first if the user didn't disable the exact alarm permission while the app was closed
        if (alarmHelper.canScheduleAlarms()) {
            mAlarmUp = false;
            savedSharedPreferences();
        }

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

        // Call setOnCheckedChangeListener() on the ToggleButton instance.
        alarmToggle.setOnClickListener(v -> {
            String toastMessage;
            if (!mAlarmUp) {
                if (alarmHelper.canScheduleAlarms()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        startActivity(intent);
                        toastMessage = getString(R.string.message_alarm_permission_needed);
                    } else {
                        setAlarm();
                        // Set the toast message for the "on" case.
                        toastMessage = getString(R.string.message_alarm_on);
                    }
                } else {
                    setAlarm();
                    // Set the toast message for the "on" case.
                    toastMessage = getString(R.string.message_alarm_on);
                }
            } else {
                // Cancel the alarm
                alarmHelper.cancelAlarm(MainActivity.this);

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
            Toast.makeText(MainActivity.this, toastMessage,Toast.LENGTH_LONG)
                    .show();

            // Save the new state to shared preferences
            savedSharedPreferences();
        });

        leftArrow.setOnClickListener(v -> {
            if (mIntervalPosition > 0 && mIntervalPosition < mIntervalList.length) {
                mIntervalPosition--;
            } else if (mIntervalPosition == 0) {
                mIntervalPosition = mIntervalList.length - 1;
            } else {
                mIntervalPosition = 0;
            }

            // set mInterval to the correct amount of milliseconds
            switch (mIntervalPosition) {
                case 1:
                    mInterval = 1800000;
                    break;
                case 2:
                    mInterval = 3600000;
                    break;
                case 3:
                    mInterval = 7200000;
                    break;
                default:
                    mInterval = 900000;
            }

            // Set the state of the interval textview
            intervalTextView.setText(mIntervalList[mIntervalPosition]);

            // If the alarm button is ON, then cancel the current alarm and set a new one
            if (mAlarmUp) {
                // Cancel the current alarm
                alarmHelper.cancelAlarm(MainActivity.this);

                // to remove the current notification
                notificationHelper.cancelNotification();

                // Set the alarm with the new interval time
                alarmHelper.setAlarm(MainActivity.this, mInterval);
            }

            // Save mIntervalPosition and mInterval to shared preferences
            savedSharedPreferences();
        });

        rightArrow.setOnClickListener(v -> {
            if (mIntervalPosition >= 0 && mIntervalPosition < mIntervalList.length -1) {
                mIntervalPosition++;
            } else if (mIntervalPosition == mIntervalList.length - 1) {
                mIntervalPosition = 0;
            } else {
                mIntervalPosition = mIntervalList.length - 1;
            }

            // set mInterval to the correct amount of milliseconds
            switch (mIntervalPosition) {
                case 1:
                    mInterval = 1800000;
                    break;
                case 2:
                    mInterval = 3600000;
                    break;
                case 3:
                    mInterval = 7200000;
                    break;
                default:
                    mInterval = 900000;
            }

            // Set the state of the interval textview
            intervalTextView.setText(mIntervalList[mIntervalPosition]);

            // If the alarm button is ON, then cancel the current alarm and set a new one
            if (mAlarmUp) {
                // Cancel the current alarm
                alarmHelper.cancelAlarm(MainActivity.this);

                // to remove the current notification
                notificationHelper.cancelNotification();

                // Set the alarm with the new interval time
                alarmHelper.setAlarm(MainActivity.this, mInterval);
            }

            // Save mIntervalPosition and mInterval to shared preferences
            savedSharedPreferences();
        });

        startHour.setOnClickListener(v -> {

            // Get current time
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Launch Time picker dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute1) -> {
                        if((hourOfDay < mStopTimeHour) || (hourOfDay == mStopTimeHour && minute1 < mStopTimeMinutes)) {
                            mStartTimeHour = hourOfDay;
                            mStartTimeMinutes = minute1;
                            setStartStopTime();
                            savedSharedPreferences();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.toast_text_wrong_starthour,Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }, hour, minute, DateFormat.is24HourFormat(this));
            timePickerDialog.show();
        });

        endHour.setOnClickListener(v -> {

            // Get current time
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Launch Time picker dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute12) -> {
                        if((hourOfDay > mStartTimeHour) || (hourOfDay == mStartTimeHour && minute12 > mStartTimeMinutes)) {
                            mStopTimeHour = hourOfDay;
                            mStopTimeMinutes = minute12;
                            setStartStopTime();
                            savedSharedPreferences();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.toast_text_wrong_endhour,Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }, hour, minute, DateFormat.is24HourFormat(this));
            timePickerDialog.show();

        });

        weekdaysOnly.setOnCheckedChangeListener((compoundButton, b) -> {
            mWeekdaysOnly = !mWeekdaysOnly;
            savedSharedPreferences();
        });

        // We call createNotificationChannel() at the end of the onCreate() method
        notificationHelper.createNotificationChannel(MainActivity.this);

    }

    private void setAlarm() {
        // Set the alarm
        alarmHelper.setAlarm(MainActivity.this, mInterval);

        // Change the button and textview appearance
        alarmToggle.setBackgroundResource(R.drawable.round_button_blue);
        alarmToggle.setImageResource(R.drawable.ic_notifications_active);
        alarmTextView.setText(R.string.alarm_on_text);

        // Save the toggleButton state to the private boolean mAlarmUp
        mAlarmUp = true;
    }

    private void setStartStopTime() {
        String startTimeHour;
        String startTimeMinutes;
        String stopTimeHour;
        String stopTimeMinutes;
        String displayedStartTime;
        String displayedStopTime;

        // Set the start en stop time for the stand up alert
        if (DateFormat.is24HourFormat(this)) {
            if (mStartTimeHour < 10) {
                startTimeHour = "0" + mStartTimeHour;
            } else {
                startTimeHour = Integer.toString(mStartTimeHour);
            }

            if (mStartTimeMinutes < 10) {
                startTimeMinutes = "0" + mStartTimeMinutes;
            } else {
                startTimeMinutes = Integer.toString(mStartTimeMinutes);
            }

            if (mStopTimeHour < 10) {
                stopTimeHour = "0" + mStopTimeHour;
            } else {
                stopTimeHour = Integer.toString(mStopTimeHour);
            }

            if (mStopTimeMinutes < 10) {
                stopTimeMinutes = "0" + mStopTimeMinutes;
            } else {
                stopTimeMinutes = Integer.toString(mStopTimeMinutes);
            }

            displayedStartTime = startTimeHour + ":" + startTimeMinutes;
            displayedStopTime = stopTimeHour + ":" + stopTimeMinutes;
            startHour.setText(displayedStartTime);
            endHour.setText(displayedStopTime);
        } else {
            if (mStartTimeHour > 0 && mStartTimeHour < 12) {

                if (mStartTimeHour < 10) {
                    startTimeHour = "0" + mStartTimeHour;
                } else {
                    startTimeHour = Integer.toString(mStartTimeHour);
                }

                if (mStartTimeMinutes < 10) {
                    startTimeMinutes = "0" + mStartTimeMinutes;
                } else {
                    startTimeMinutes = Integer.toString(mStartTimeMinutes);
                }

                displayedStartTime = startTimeHour + ":" + startTimeMinutes + " AM";

            } else if (mStartTimeHour == 0) {

                if (mStartTimeMinutes < 10) {
                    startTimeMinutes = "0" + mStartTimeMinutes;
                } else {
                    startTimeMinutes = Integer.toString(mStartTimeMinutes);
                }

                displayedStartTime = "12:" + startTimeMinutes + " AM";

            } else if (mStartTimeHour > 12 && mStartTimeHour < 24) {

                int convertedTime = mStartTimeHour - 12;

                if (convertedTime < 10) {
                    startTimeHour = "0" + convertedTime;
                } else {
                    startTimeHour = Integer.toString(convertedTime);
                }

                if (mStartTimeMinutes < 10) {
                    startTimeMinutes = "0" + mStartTimeMinutes;
                } else {
                    startTimeMinutes = Integer.toString(mStartTimeMinutes);
                }

                displayedStartTime = startTimeHour + ":" + startTimeMinutes + " PM";

            } else {

                if (mStartTimeMinutes < 10) {
                    startTimeMinutes = "0" + mStartTimeMinutes;
                } else {
                    startTimeMinutes = Integer.toString(mStartTimeMinutes);
                }

                displayedStartTime = mStartTimeHour + ":" + startTimeMinutes + " PM";

            }

            startHour.setText(displayedStartTime);

            if (mStopTimeHour > 0 && mStopTimeHour < 12) {

                if (mStopTimeHour < 10) {
                    stopTimeHour = "0" + mStopTimeHour;
                } else {
                    stopTimeHour = Integer.toString(mStopTimeHour);
                }

                if (mStopTimeMinutes < 10) {
                    stopTimeMinutes = "0" + mStopTimeMinutes;
                } else {
                    stopTimeMinutes = Integer.toString(mStopTimeMinutes);
                }

                displayedStopTime = stopTimeHour + ":" + stopTimeMinutes + " AM";

            } else if (mStopTimeHour == 0) {

                if (mStopTimeMinutes < 10) {
                    stopTimeMinutes = "0" + mStopTimeMinutes;
                } else {
                    stopTimeMinutes = Integer.toString(mStopTimeMinutes);
                }

                displayedStopTime = "12:" + stopTimeMinutes + " AM";

            } else if (mStopTimeHour > 12 && mStopTimeMinutes < 24) {

                int convertedTime = mStopTimeHour - 12;

                if (convertedTime < 10) {
                    stopTimeHour = "0" + convertedTime;
                } else {
                    stopTimeHour = Integer.toString(convertedTime);
                }

                if (mStopTimeMinutes < 10) {
                    stopTimeMinutes = "0" + mStopTimeMinutes;
                } else {
                    stopTimeMinutes = Integer.toString(mStopTimeMinutes);
                }

                displayedStopTime = stopTimeHour + ":" + stopTimeMinutes + " PM";

            } else {

                if (mStopTimeMinutes < 10) {
                    stopTimeMinutes = "0" + mStopTimeMinutes;
                } else {
                    stopTimeMinutes = Integer.toString(mStopTimeMinutes);
                }

                displayedStopTime = mStopTimeHour + ":" + stopTimeMinutes + " PM";

            }

            endHour.setText(displayedStopTime);
        }
    }

    private void savedSharedPreferences() {

        // get and editor for the SharePreferences object
        mSharedPreferences = getSharedPreferences(mSharedPreferencesFile, MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = mSharedPreferences.edit();
        sharedPreferencesEditor.putBoolean("ALARM_UP", mAlarmUp);
        sharedPreferencesEditor.putBoolean("WEEKDAYS_ONLY", mWeekdaysOnly);
        sharedPreferencesEditor.putInt("INTERVAL", mInterval);
        sharedPreferencesEditor.putInt("INTERVAL_POS", mIntervalPosition);
        sharedPreferencesEditor.putInt("START_HOURS", mStartTimeHour);
        sharedPreferencesEditor.putInt("START_MINUTES", mStartTimeMinutes);
        sharedPreferencesEditor.putInt("STOP_HOURS", mStopTimeHour);
        sharedPreferencesEditor.putInt("STOP_MINUTES", mStopTimeMinutes);
        sharedPreferencesEditor.apply();

    }
}