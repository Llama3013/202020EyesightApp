package com.test.a202020app;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Objects;

import static java.lang.Math.toIntExact;

// TODO: Make the UX better in general
// TODO: Comment code
// TODO: Add work hours
// TODO: Clean and revise media alarm sound picker
// TODO: Find out if a service would work to stop the dozing
// TODO: Try to get exact alarm option to use foreground service and inexact alarm option to use setExactWhileIdle
// TODO: setExactWhileIdle supposedly will be up to 11 minutes early/late
// TODO: Notifications need work (AlarmReceiver notification doesn't work at all)

public class MainActivity extends AppCompatActivity {
    private final String CHANNEL_ID = "202020App";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Log.d("debug", "MainActivity: OnCreate");

        Intent notificationIntent = new Intent(getBaseContext(), AlarmReceiver.class);
        String packageName = this.getPackageName();
        /*
          The next about 9 lines of code are trying to access the PowerManager and trying to ignore
          battery optimizations which if the play store finds that it isn't necessary they will remove
          it from the play store.
         */
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //Log.d("debug", "permission for ignoring battery optimizations is " + pm.isIgnoringBatteryOptimizations(packageName));
            if (pm.isIgnoringBatteryOptimizations(packageName))
                notificationIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            else {
                notificationIntent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                notificationIntent.setData(Uri.parse("package:" + packageName));
            }
        }

        //
        final PendingIntent broadcast = PendingIntent.getBroadcast(getApplicationContext(),
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getBoolean("newAlarm")) {
                startTimer(broadcast);
                //Log.d("debug", "Alarm should be active");
            } else if (!bundle.getBoolean("newAlarm")) {
                stopTimer(broadcast);
                //Log.d("debug", "Alarm should not be active");
            }
        }

        setupUI(broadcast);
    }

    /**
     * This calculates the time left until the alarm is due
     *
     * @return returns a formatted string of the time left
     */
    private String updateTime() {
        SharedPreferences settings = getSharedPreferences("setting", 0);
        long currentTime = System.currentTimeMillis();
        long startTime = settings.getLong("timeStart", 0);
        long time = settings.getLong("timer", 200000);
        long finishAlarmTime = startTime + time;
        long timeLeft = finishAlarmTime - currentTime;
        long minutes = timeLeft / 60000;
        long seconds = (timeLeft / 1000) % 60 + 1;
        String formattedTime;
        if (seconds < 10 && seconds >= 0) {
            formattedTime = minutes + " : " + 0 + seconds;
        } else if (seconds == 60) {
            minutes += 1;
            seconds = 0;
            formattedTime = minutes + " : " + 0 + seconds;
        } else if (seconds >= 10) {
            formattedTime = minutes + " : " + seconds;
        } else {
            formattedTime = getString(R.string.done);
        }
        return formattedTime;
    }

    /**
     * This sets up the notificationChannel for the notification that will be shown when the timer
     * is running. (If the user selected the notification option)
     */
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * This starts the notification that will be shown when the timer is running. (If the user
     * selected the notification option)
     */
    private void startNotification() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 1, intent, 0);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("202020 App")
                .setContentText("Alarm is running")
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_ALARM);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, builder.build());
    }

    //This is used for checking sharedPreferences
    /*
    private void checkSettings() {
        SharedPreferences settings = getSharedPreferences("setting", 0);
        Long time = settings.getLong("timer", 999);
        Log.d("Test", time.toString());
        Boolean timerRunning = settings.getBoolean("timerRunning", false);
        Log.d("Test", timerRunning.toString());
        String vibe = settings.getString("vibrations", "error");
        Log.d("Test", vibe);
        String sound = settings.getString("soundFile", "error");
        Log.d("Test", sound);
        String alarmType = settings.getString("alarmType", "error");
        Log.d("Test", alarmType);
        Long reset = settings.getLong("resetTime", 999);
        Log.d("Test", reset.toString());
        String notification = settings.getString("notifications", "error");
        Log.d("Test", notification);
    }*/

    /**
     * Changes shared preferences to say the timer is running and records the time the the timer
     * started. It then starts the timer based on settings. Lastly it changes the ui.
     *
     * @param broadcast This is the PendingIntent for the alarm that will be called.
     */
    private void startTimer(PendingIntent broadcast) {
        //final Toast startToast = Toast.makeText(this, "Timer started", Toast.LENGTH_SHORT);

        //This loads SharedPreferences
        SharedPreferences settings = getSharedPreferences("setting", 0);
        if (settings.getBoolean("timerRunning", false)) {
            stopTimer(broadcast);
        }

        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean("timerRunning", true);
        editor.putLong("timeStart", System.currentTimeMillis());
        editor.apply();

        long time = settings.getLong("timer", 200000);
        String alarmType = settings.getString("alarmType", "exact");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmType == "inexact") {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + time, broadcast);
                Log.d("debug", "Main activity: alarm inexact");
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        System.currentTimeMillis() + time, broadcast);
                Log.d("debug", "Main activity: alarm exact");
            }
        }

        String notification = settings.getString("notifications", "on");

        if (Objects.equals(notification, "on")) {
            startNotification();
        }
        alarmOnUI();

        //startToast.show();

    }

    /**
     * Changes shared preferences to say the timer is not running and cancels the timer that is set.
     * It then changes the ui and cancels the timer notification (if the notification it is active)
     *
     * @param broadcast This is the PendingIntent for the alarm that will be called.
     */
    private void stopTimer(PendingIntent broadcast) {
        //final Toast stopToast = Toast.makeText(this, "Timer stopped", Toast.LENGTH_SHORT);

        //This loads SharedPreferences
        SharedPreferences settings = getSharedPreferences("setting", 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean("timerRunning", false);
        editor.apply();
        final AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(broadcast);
        alarmOffUI();

        //stopToast.show();

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    /**
     * This prepares the MainActivity screen for use and checks if the timer is running or not to
     * show the correct ui.
     *
     * @param broadcast This is the PendingIntent for the alarm that will be called.
     */
    private void setupUI(final PendingIntent broadcast) {
        //This loads SharedPreferences
        SharedPreferences settings = getSharedPreferences("setting", 0);

        createNotificationChannel();

        //region Setup for number picker for minutes until alarm
        //converts the timer time to minutes so the number picker can read it
        long microSeconds = settings.getLong("timer", 200000);
        int minutes;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            minutes = toIntExact(microSeconds) / 1000 / 60;
        } else {
            minutes = (int) microSeconds / 1000 / 60;
        }

        final NumberPicker npTime = findViewById(R.id.activity_main_np_time_interval);
        //This sets the bounds of the timer they can set it to 1 minute to 60 minutes
        npTime.setMinValue(1);
        npTime.setMaxValue(60);
        npTime.setWrapSelectorWheel(true);
        //This will set number picker to the previous set minutes
        npTime.setValue(minutes);
        npTime.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            /**
             * This changes the shared preferences of timer to the new value
             * @param npTime This is the NumberPicker
             * @param oldVal This is the old value of the number picker
             * @param newVal This is the new value of the number picker
             */
            @Override
            public void onValueChange(NumberPicker npTime, int oldVal, int newVal) {
                long microSeconds = newVal * 1000 * 60;
                SharedPreferences settings = getSharedPreferences("setting", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putLong("timer", microSeconds);
                editor.apply();
            }
        });
        //endregion


        //region Setup for user interaction based on current state
        /*
         This line checks for if the timer is running and if it is running it will change the
         text of the start button to stop button, disable the number picker and minutes text and
         enable the timer countdown and reset button
        */
        if (settings.getBoolean("timerRunning", false)) {
            alarmOnUI();
            /*
             If the timer isn't running it will change the text of the stop button to start button,
             enable the number picker and minutes text and disable the timer countdown and reset button
            */
        } else {
            alarmOffUI();

        }
        //endregion

        //region Setup for onClick for start stop button

        final Button btnStartStop = findViewById(R.id.activity_main_btn_start);
        btnStartStop.setOnClickListener(new View.OnClickListener() {
            /**
             * This activates when the user taps activity_main_btn_start which will start or stop the timer
             *
             * @param v this gives context of the view
             */
            public void onClick(View v) {
                //This loads SharedPreferences
                SharedPreferences settings = getSharedPreferences("setting", 0);
                /* This line checks for if the timer is running and if it is running it will change the
                   text of the stop button to start button, enable the number picker and minutes text,
                   disable the timer countdown and reset button, stop the foreground service using
                   stopServiceIntent and show a toast saying "timer stopped" */
                if (settings.getBoolean("timerRunning", false)) {
                    stopTimer(broadcast);
                /* This line checks for if the timer isn't running and if it isn't running it will
                   change the text of the start button to stop button, disable the number picker
                   and minutes text, enable the timer countdown and reset button, start the
                   foreground service using startServiceIntent and show a toast saying "timer
                   started" */
                } else {
                    startTimer(broadcast);
                }
            }
        });
        //endregion

        final Button btnReset = findViewById(R.id.activity_main_btn_reset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            /**
             * This activates when the user taps activity_main_btn_start which will start or stop the timer
             *
             * @param v this gives context of the view
             */
            public void onClick(View v) {
                startTimer(broadcast);
            }
        });

        //region Setup for the text view timer to update the time
        final TextView tvTimer = findViewById(R.id.activity_main_tv_timer);
        tvTimer.post(new Runnable() {
            @Override
            public void run() {
                SharedPreferences settings = getSharedPreferences("setting", 0);
                if (settings.getBoolean("timerRunning", false)) {
                    tvTimer.setText(updateTime());
                }
                tvTimer.postDelayed(this, 500);
            }
        });
        //endregion
    }

    /**
     * This will show that the alarm is not running and also disable/enable certain ui elements
     */
    private void alarmOffUI() {
        final NumberPicker npTime = findViewById(R.id.activity_main_np_time_interval);
        final TextView tvTimer = findViewById(R.id.activity_main_tv_timer);
        final TextView tvMinutes = findViewById(R.id.activity_main_tv_mins);
        final Button btnStartStop = findViewById(R.id.activity_main_btn_start);
        final Button btnReset = findViewById(R.id.activity_main_btn_reset);
        npTime.setEnabled(true);
        tvMinutes.setEnabled(true);
        tvTimer.setText(R.string.waiting);
        tvTimer.setEnabled(false);
        btnReset.setEnabled(false);
        btnStartStop.setText(getString(R.string.start));
        btnStartStop.setBackgroundTintList(getResources().getColorStateList(R.color.colorGreen,
                null));
    }

    /**
     * This will show that the alarm is running and also disable/enable certain ui elements
     */
    private void alarmOnUI() {
        final NumberPicker npTime = findViewById(R.id.activity_main_np_time_interval);
        final TextView tvTimer = findViewById(R.id.activity_main_tv_timer);
        final TextView tvMinutes = findViewById(R.id.activity_main_tv_mins);
        final Button btnStartStop = findViewById(R.id.activity_main_btn_start);
        final Button btnReset = findViewById(R.id.activity_main_btn_reset);
        npTime.setEnabled(false);
        tvMinutes.setEnabled(false);
        tvTimer.setEnabled(true);
        btnReset.setEnabled(true);
        btnStartStop.setText(getString(R.string.stop));
        btnStartStop.setBackgroundTintList(getResources().getColorStateList(R.color.colorRed,
                null));
    }

    /**
     * When the user taps on the button settings it will start the settings activity
     */
    public void settings(View view) {
        //checkSettings();
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    /**
     * When the user taps on the button help it will start the help activity
     */
    public void help(View view) {
        Intent helpIntent = new Intent(this, HelpActivity.class);
        startActivity(helpIntent);
    }

    /**
     * This tries to save to SharedPreferences that the timer has stopped running when it is destroyed
     */
    /*@Override
    public void onDestroy() {
        SharedPreferences settings = getSharedPreferences("setting", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("timerRunning", false);
        editor.apply();
        super.onDestroy();
    }*/

    /**
     * This stops the user from using back button to make sure the user doesn't create more timers
     * I was thinking that maybe this should just make the user go back to home
     */
    @Override
    public void onBackPressed() {
        //Do Nothing
    }

}
