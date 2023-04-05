package com.thpir.standup

import android.app.TimePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    // Private boolean that show whether the alarm is on or off
    private var mAlarmUp = false
    private var mWeekdaysOnly = false
    private var mInterval = 3600000
    private var mIntervalPosition = 2
    private var mStartTimeHour = 0
    private var mStartTimeMinutes = 0
    private var mStopTimeHour = 0
    private var mStopTimeMinutes = 0
    private lateinit var alarmToggle: ImageButton
    private lateinit var alarmTextView: TextView
    private lateinit var startHour: TextView
    private lateinit var endHour: TextView
    private val mIntervalList = arrayOf("15 Minutes", "30 Minutes", "1 Hour", "2 Hours")

    // Shared Preferences
    private lateinit var  mSharedPreferences: SharedPreferences
    private val mSharedPreferencesFile = "com.thpir.standup"

    // Create an instance of the AlarmHelper class
    private var alarmHelper = AlarmHelper()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the shared preferences
        mSharedPreferences = getSharedPreferences(mSharedPreferencesFile, MODE_PRIVATE)
        mAlarmUp = mSharedPreferences.getBoolean("ALARM_UP", false)
        mWeekdaysOnly = mSharedPreferences.getBoolean("WEEKDAYS_ONLY", false)
        mInterval = mSharedPreferences.getInt("INTERVAL", 3600000)
        mIntervalPosition = mSharedPreferences.getInt("INTERVAL_POS", 2)
        mStartTimeHour = mSharedPreferences.getInt("START_HOURS", 9)
        mStartTimeMinutes = mSharedPreferences.getInt("START_MINUTES", 0)
        mStopTimeHour = mSharedPreferences.getInt("STOP_HOURS", 18)
        mStopTimeMinutes = mSharedPreferences.getInt("STOP_MINUTES", 0)

        // Initialize the views of the MainActivity
        val intervalTextView = findViewById<TextView>(R.id.textViewInterval)
        startHour = findViewById(R.id.textViewFromTime)
        endHour = findViewById(R.id.textViewUntilTime)
        val leftArrow = findViewById<ImageView>(R.id.imageViewToggleLeft)
        val rightArrow = findViewById<ImageView>(R.id.imageViewToggleRight)
        val weekdaysOnly = findViewById<CheckBox>(R.id.checkBoxWeekdays)
        alarmToggle = findViewById(R.id.imageButtonAlarm)
        alarmTextView = findViewById(R.id.textViewAlarm)

        // Set the saved start and stop time
        setStartStopTime()

        // Set the weekdays only checkbox
        weekdaysOnly.isChecked = mWeekdaysOnly

        // Set the state of the interval textview
        intervalTextView.text = mIntervalList[mIntervalPosition]

        // Create an instance of the NotificationHelper class
        val notificationHelper = NotificationHelper()

        // Create a notification
        notificationHelper.createNotificationManager(this@MainActivity)

        // Create an alarm
        alarmHelper.createAlarm(this@MainActivity)

        // When we open the app we want to check first if the user didn't disable the exact alarm permission while the app was closed
        if (alarmHelper.canScheduleAlarms()) {
            mAlarmUp = false
            savedSharedPreferences()
        }

        // Set the state of the toggle
        if (mAlarmUp) {
            alarmToggle.setBackgroundResource(R.drawable.round_button_blue)
            alarmToggle.setImageResource(R.drawable.ic_notifications_active)
            alarmTextView.setText(R.string.alarm_on_text)
        } else {
            alarmToggle.setBackgroundResource(R.drawable.round_button_grey)
            alarmToggle.setImageResource(R.drawable.ic_notifications_off)
            alarmTextView.setText(R.string.alarm_off_text)
        }

        // Call setOnCheckedChangeListener() on the ToggleButton instance.
        alarmToggle.setOnClickListener {
            val toastMessage: String
            if (!mAlarmUp) {
                toastMessage = if (alarmHelper.canScheduleAlarms()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        startActivity(intent)
                        getString(R.string.message_alarm_permission_needed)
                    } else {
                        setAlarm()
                        // Set the toast message for the "on" case.
                        getString(R.string.message_alarm_on)
                    }
                } else {
                    setAlarm()
                    // Set the toast message for the "on" case.
                    getString(R.string.message_alarm_on)
                }
            } else {
                // Cancel the alarm
                alarmHelper.cancelAlarm(this@MainActivity)

                // We call cancelAll() on the NotificationManager if the toggle is turned off
                // to remove the notification
                notificationHelper.cancelNotification()

                // Set the toast message for the "off" case.
                toastMessage = getString(R.string.message_alarm_off)

                // Change the button and textview appearance
                alarmToggle.setBackgroundResource(R.drawable.round_button_grey)
                alarmToggle.setImageResource(R.drawable.ic_notifications_off)
                alarmTextView.setText(R.string.alarm_off_text)

                // Save the toggleButton state to the private boolean mAlarmUp
                mAlarmUp = false
            }

            // Show a toast to say the alarm is turned on or off.
            Toast.makeText(this@MainActivity, toastMessage, Toast.LENGTH_LONG)
                .show()

            // Save the new state to shared preferences
            savedSharedPreferences()
        }
        leftArrow.setOnClickListener {
            if (mIntervalPosition > 0 && mIntervalPosition < mIntervalList.size) {
                mIntervalPosition--
            } else if (mIntervalPosition == 0) {
                mIntervalPosition = mIntervalList.size - 1
            } else {
                mIntervalPosition = 0
            }
            mInterval = when (mIntervalPosition) {
                1 -> 1800000
                2 -> 3600000
                3 -> 7200000
                else -> 900000
            }

            // Set the state of the interval textview
            intervalTextView.text = mIntervalList[mIntervalPosition]

            // If the alarm button is ON, then cancel the current alarm and set a new one
            if (mAlarmUp) {
                // Cancel the current alarm
                alarmHelper.cancelAlarm(this@MainActivity)

                // to remove the current notification
                notificationHelper.cancelNotification()

                // Set the alarm with the new interval time
                alarmHelper.setAlarm(this@MainActivity, mInterval)
            }

            // Save mIntervalPosition and mInterval to shared preferences
            savedSharedPreferences()
        }
        rightArrow.setOnClickListener {
            if (mIntervalPosition >= 0 && mIntervalPosition < mIntervalList.size - 1) {
                mIntervalPosition++
            } else if (mIntervalPosition == mIntervalList.size - 1) {
                mIntervalPosition = 0
            } else {
                mIntervalPosition = mIntervalList.size - 1
            }
            mInterval = when (mIntervalPosition) {
                1 -> 1800000
                2 -> 3600000
                3 -> 7200000
                else -> 900000
            }

            // Set the state of the interval textview
            intervalTextView.text = mIntervalList[mIntervalPosition]

            // If the alarm button is ON, then cancel the current alarm and set a new one
            if (mAlarmUp) {
                // Cancel the current alarm
                alarmHelper.cancelAlarm(this@MainActivity)

                // to remove the current notification
                notificationHelper.cancelNotification()

                // Set the alarm with the new interval time
                alarmHelper.setAlarm(this@MainActivity, mInterval)
            }

            // Save mIntervalPosition and mInterval to shared preferences
            savedSharedPreferences()
        }
        startHour.setOnClickListener {

            // Get current time
            val calendar = Calendar.getInstance()
            val hour = calendar[Calendar.HOUR_OF_DAY]
            val minute = calendar[Calendar.MINUTE]

            // Launch Time picker dialog
            val timePickerDialog = TimePickerDialog(
                this,
                { _: TimePicker?, hourOfDay: Int, minute1: Int ->
                    if (hourOfDay < mStopTimeHour || hourOfDay == mStopTimeHour && minute1 < mStopTimeMinutes) {
                        mStartTimeHour = hourOfDay
                        mStartTimeMinutes = minute1
                        setStartStopTime()
                        savedSharedPreferences()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.toast_text_wrong_starthour,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }, hour, minute, DateFormat.is24HourFormat(this)
            )
            timePickerDialog.show()
        }
        endHour.setOnClickListener {

            // Get current time
            val calendar = Calendar.getInstance()
            val hour = calendar[Calendar.HOUR_OF_DAY]
            val minute = calendar[Calendar.MINUTE]

            // Launch Time picker dialog
            val timePickerDialog = TimePickerDialog(
                this,
                { _: TimePicker?, hourOfDay: Int, minute12: Int ->
                    if (hourOfDay > mStartTimeHour || hourOfDay == mStartTimeHour && minute12 > mStartTimeMinutes) {
                        mStopTimeHour = hourOfDay
                        mStopTimeMinutes = minute12
                        setStartStopTime()
                        savedSharedPreferences()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.toast_text_wrong_endhour,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }, hour, minute, DateFormat.is24HourFormat(this)
            )
            timePickerDialog.show()
        }
        weekdaysOnly.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
            mWeekdaysOnly = !mWeekdaysOnly
            savedSharedPreferences()
        }

        // We call createNotificationChannel() at the end of the onCreate() method
        notificationHelper.createNotificationChannel(this@MainActivity)
    }

    private fun setAlarm() {
        // Set the alarm
        alarmHelper.setAlarm(this@MainActivity, mInterval)

        // Change the button and textview appearance
        alarmToggle.setBackgroundResource(R.drawable.round_button_blue)
        alarmToggle.setImageResource(R.drawable.ic_notifications_active)
        alarmTextView.setText(R.string.alarm_on_text)

        // Save the toggleButton state to the private boolean mAlarmUp
        mAlarmUp = true
    }

    private fun setStartStopTime() {
        val startTimeHour: String
        val startTimeMinutes: String
        val stopTimeHour: String
        val stopTimeMinutes: String
        val displayedStartTime: String
        val displayedStopTime: String

        // Set the start en stop time for the stand up alert
        if (DateFormat.is24HourFormat(this)) {
            startTimeHour = if (mStartTimeHour < 10) {
                "0$mStartTimeHour"
            } else {
                mStartTimeHour.toString()
            }
            startTimeMinutes = if (mStartTimeMinutes < 10) {
                "0$mStartTimeMinutes"
            } else {
                mStartTimeMinutes.toString()
            }
            stopTimeHour = if (mStopTimeHour < 10) {
                "0$mStopTimeHour"
            } else {
                mStopTimeHour.toString()
            }
            stopTimeMinutes = if (mStopTimeMinutes < 10) {
                "0$mStopTimeMinutes"
            } else {
                mStopTimeMinutes.toString()
            }
            displayedStartTime = "$startTimeHour:$startTimeMinutes"
            displayedStopTime = "$stopTimeHour:$stopTimeMinutes"
            startHour.text = displayedStartTime
            endHour.text = displayedStopTime
        } else {
            when (mStartTimeHour) {
                in 1..11 -> {
                    startTimeHour = if (mStartTimeHour < 10) {
                        "0$mStartTimeHour"
                    } else {
                        mStartTimeHour.toString()
                    }
                    startTimeMinutes = if (mStartTimeMinutes < 10) {
                        "0$mStartTimeMinutes"
                    } else {
                        mStartTimeMinutes.toString()
                    }
                    displayedStartTime = "$startTimeHour:$startTimeMinutes AM"
                }
                0 -> {
                    startTimeMinutes = if (mStartTimeMinutes < 10) {
                        "0$mStartTimeMinutes"
                    } else {
                        mStartTimeMinutes.toString()
                    }
                    displayedStartTime = "12:$startTimeMinutes AM"
                }
                in 13..23 -> {
                    val convertedTime = mStartTimeHour - 12
                    startTimeHour = if (convertedTime < 10) {
                        "0$convertedTime"
                    } else {
                        convertedTime.toString()
                    }
                    startTimeMinutes = if (mStartTimeMinutes < 10) {
                        "0$mStartTimeMinutes"
                    } else {
                        mStartTimeMinutes.toString()
                    }
                    displayedStartTime = "$startTimeHour:$startTimeMinutes PM"
                }
                else -> {
                    startTimeMinutes = if (mStartTimeMinutes < 10) {
                        "0$mStartTimeMinutes"
                    } else {
                        mStartTimeMinutes.toString()
                    }
                    displayedStartTime = "$mStartTimeHour:$startTimeMinutes PM"
                }
            }
            startHour.text = displayedStartTime
            if (mStopTimeHour in 1..11) {
                stopTimeHour = if (mStopTimeHour < 10) {
                    "0$mStopTimeHour"
                } else {
                    mStopTimeHour.toString()
                }
                stopTimeMinutes = if (mStopTimeMinutes < 10) {
                    "0$mStopTimeMinutes"
                } else {
                    mStopTimeMinutes.toString()
                }
                displayedStopTime = "$stopTimeHour:$stopTimeMinutes AM"
            } else if (mStopTimeHour == 0) {
                stopTimeMinutes = if (mStopTimeMinutes < 10) {
                    "0$mStopTimeMinutes"
                } else {
                    mStopTimeMinutes.toString()
                }
                displayedStopTime = "12:$stopTimeMinutes AM"
            } else if (mStopTimeHour > 12 && mStopTimeMinutes < 24) {
                val convertedTime = mStopTimeHour - 12
                stopTimeHour = if (convertedTime < 10) {
                    "0$convertedTime"
                } else {
                    convertedTime.toString()
                }
                stopTimeMinutes = if (mStopTimeMinutes < 10) {
                    "0$mStopTimeMinutes"
                } else {
                    mStopTimeMinutes.toString()
                }
                displayedStopTime = "$stopTimeHour:$stopTimeMinutes PM"
            } else {
                stopTimeMinutes = if (mStopTimeMinutes < 10) {
                    "0$mStopTimeMinutes"
                } else {
                    mStopTimeMinutes.toString()
                }
                displayedStopTime = "$mStopTimeHour:$stopTimeMinutes PM"
            }
            endHour.text = displayedStopTime
        }
    }

    private fun savedSharedPreferences() {

        // get and editor for the SharePreferences object
        mSharedPreferences = getSharedPreferences(mSharedPreferencesFile, MODE_PRIVATE)
        val sharedPreferencesEditor = mSharedPreferences.edit()
        sharedPreferencesEditor.putBoolean("ALARM_UP", mAlarmUp)
        sharedPreferencesEditor.putBoolean("WEEKDAYS_ONLY", mWeekdaysOnly)
        sharedPreferencesEditor.putInt("INTERVAL", mInterval)
        sharedPreferencesEditor.putInt("INTERVAL_POS", mIntervalPosition)
        sharedPreferencesEditor.putInt("START_HOURS", mStartTimeHour)
        sharedPreferencesEditor.putInt("START_MINUTES", mStartTimeMinutes)
        sharedPreferencesEditor.putInt("STOP_HOURS", mStopTimeHour)
        sharedPreferencesEditor.putInt("STOP_MINUTES", mStopTimeMinutes)
        sharedPreferencesEditor.apply()
    }
}