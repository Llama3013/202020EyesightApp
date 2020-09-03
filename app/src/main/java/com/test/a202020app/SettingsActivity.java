package com.test.a202020app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Objects;

import static java.lang.Math.toIntExact;

public class SettingsActivity extends AppCompatActivity {

    private final String CHANNEL_ID = "202020App";
    private final int AUDIO_REQUEST = 0;
    private String musicPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences settings = getSharedPreferences("setting", 0);
        //These two lines will find all of the radio buttons for alarm type
        final RadioButton rbExact = findViewById(R.id.activity_settings_rb_alarm_exact);
        final RadioButton rbInexact = findViewById(R.id.activity_settings_rb_alarm_inexact);
        //This will load which alarm type setting is saved in SharedPreferences
        String defaultAlarmTypeRB = settings.getString("alarmType", "exact");
        //This switch will find which alarm type setting was saved and check the radio button
        switch (Objects.requireNonNull(defaultAlarmTypeRB)) {
            case "inexact":
                rbInexact.setChecked(true);
                break;
            case "exact":
                rbExact.setChecked(true);
                break;
            default:
                rbExact.setChecked(true);
                final Toast alarmSetErrorToast = Toast.makeText(this,
                        "Error: Alarm Type wasn't set", Toast.LENGTH_SHORT);
                alarmSetErrorToast.show();
                break;
        }

        //These three lines will find all of the radio buttons for sound files
        final RadioButton rbDefault = findViewById(R.id.activity_settings_rb_default);
        final RadioButton rbNotification = findViewById(R.id.activity_settings_rb_notification);
        final RadioButton rbChooseSound = findViewById(R.id.activity_settings_rb_sound_choose);
        //This will load which sound file setting is saved in SharedPreferences
        final String defaultSoundFileRB = settings.getString("soundFile", "alarm");
        //This switch will find which sound file setting was saved and check the radio button
        switch (Objects.requireNonNull(defaultSoundFileRB)) {
            case "notification":
                rbNotification.setChecked(true);
                break;
            case "alarm":
                rbDefault.setChecked(true);
                break;
            case "choose":
                rbChooseSound.setChecked(true);
                break;
            default:
                rbDefault.setChecked(true);
                final Toast SoundSetErrorToast = Toast.makeText(this,
                        defaultSoundFileRB, Toast.LENGTH_SHORT);
                SoundSetErrorToast.show();
                break;
        }
        //
        final Button btnChoose = findViewById(R.id.activity_settings_btn_sound_choose);
        btnChoose.setOnClickListener(new View.OnClickListener() {
            /**
             *
             * @param v this gives context of the view
             */
            public void onClick(View v) {
                openGalleryForAudio();
            }
        });

        //These four lines will find all of the radio buttons for vibrations
        final RadioButton rbShort = findViewById(R.id.activity_settings_rb_short_vibration);
        final RadioButton rbMedium = findViewById(R.id.activity_settings_rb_medium_vibration);
        final RadioButton rbLong = findViewById(R.id.activity_settings_rb_long_vibration);
        final RadioButton rbNone = findViewById(R.id.activity_settings_rb_none_vibration);
        //This will load which vibration setting is saved in SharedPreferences
        String defaultVibrateRB = settings.getString("vibrations", "none");
        //This switch will find which vibration setting was saved and check the radio button
        switch (Objects.requireNonNull(defaultVibrateRB)) {
            case "short":
                rbShort.setChecked(true);
                break;
            case "medium":
                rbMedium.setChecked(true);
                break;
            case "long":
                rbLong.setChecked(true);
                break;
            case "none":
                rbNone.setChecked(true);
                break;
            default:
                rbNone.setChecked(true);
                rbShort.setChecked(false);
                rbMedium.setChecked(false);
                rbLong.setChecked(false);
                final Toast vibrateSetErrorToast = Toast.makeText(this,
                        "Error: Vibrate wasn't set", Toast.LENGTH_SHORT);
                vibrateSetErrorToast.show();
                break;
        }

        //These three lines will find the two radio buttons and number picker for reset time
        final RadioButton rbResetSet = findViewById(R.id.activity_settings_rb_reset_time_set);
        final RadioButton rbResetInfinite = findViewById(R.id.activity_settings_rb_reset_time_infinite);
        final NumberPicker npTime = findViewById(R.id.activity_settings_np_reset_time);
        //This will load which reset time setting is saved in SharedPreferences
        long resetTime = settings.getLong("resetTime", 20000);
        //This if will find what reset time setting was saved, check the correct radio button
        //depending on the value
        if (resetTime > 0) {
            rbResetSet.setChecked(true);
            npTime.setEnabled(true);
        } else if (resetTime == 0) {
            rbResetInfinite.setChecked(true);
            npTime.setEnabled(false);
        } else {
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("resetTime", 20000);
            editor.apply();
            rbResetSet.setChecked(true);
            npTime.setEnabled(true);
            final Toast vibrateSetErrorToast = Toast.makeText(this,
                    "Error: Reset time wasn't set", Toast.LENGTH_SHORT);
            vibrateSetErrorToast.show();
        }

        long microSeconds = resetTime;
        //This if statement checks if the reset time is infinite and then sets the time to 20 mins
        //Probs needs to change this
        if (rbResetInfinite.isChecked()) {
            microSeconds = 200000;
        }
        int seconds;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            seconds = toIntExact(microSeconds) / 1000;
        } else {
            seconds = (int) microSeconds / 1000;
        }
        //This sets the bounds of the reset time they can set it to 1 second to 60 seconds
        npTime.setMinValue(1);
        npTime.setMaxValue(60);
        npTime.setWrapSelectorWheel(true);
        //This will set number picker to the previous set seconds
        npTime.setValue(seconds);

        final RadioGroup rgReset = findViewById(R.id.activity_settings_rg_reset_time);
        rgReset.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (rbResetSet.isChecked()) {
                    npTime.setEnabled(true);
                } else if (rbResetInfinite.isChecked()) {
                    npTime.setEnabled(false);
                }
            }
        });

        //These two lines will find all of the radio buttons for notification settings
        final RadioButton rbNotificationOn = findViewById(R.id.activity_settings_rb_notification_on);
        final RadioButton rbNotificationOff = findViewById(R.id.activity_settings_rb_notification_off);
        //This will load which sound file setting is saved in SharedPreferences
        String defaultNotification = settings.getString("notifications", "on");
        //This switch will find which notification setting was saved and check the radio button
        switch (Objects.requireNonNull(defaultNotification)) {
            case "on":
                rbNotificationOn.setChecked(true);
                break;
            case "off":
                rbNotificationOff.setChecked(true);
                break;
            default:
                rbNotificationOn.setChecked(true);
                final Toast alarmSetErrorToast = Toast.makeText(this,
                        "Error: Notification wasn't set", Toast.LENGTH_SHORT);
                alarmSetErrorToast.show();
                break;
        }
    }

    /**
     * This should be called when the openGalleryForAudio function has been called and the user has
     * selected an option. This converts the
     * @param requestCode This is the int supplied by startActivityForResult() to identify where
     *                    the result came from
     * @param resultCode This gives a code to tell if the activity has crashed or worked correctly
     * @param data This is the sound file's data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUDIO_REQUEST && data != null) {
            Uri uri = data.getData();
            musicPath = Objects.requireNonNull(uri).toString();
            //Toast.makeText(SettingsActivity.this, musicPath, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *
     */
    private void openGalleryForAudio() {
        Intent audioIntent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(audioIntent, "Select Audio"), AUDIO_REQUEST);
    }

    /**
     * When the user taps on the button back it will start the main activity and toasts saying
     * "Settings discarded"
     */
    public void back(View view) {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        /*Toast settingsLostToast = Toast.makeText(this, "Settings discarded",
                Toast.LENGTH_SHORT);
        settingsLostToast.show();*/
    }

    /**
     * When the user taps on the button settings it will start the settings activity, saves any
     * changes that were made on the settings activity and toasts saying "Settings saved"
     */
    public void confirm(View view) {
        //These six lines will find all of the radio buttons for the settings activity
        final RadioButton rbNone = findViewById(R.id.activity_settings_rb_none_vibration);
        final RadioButton rbShort = findViewById(R.id.activity_settings_rb_short_vibration);
        final RadioButton rbMedium = findViewById(R.id.activity_settings_rb_medium_vibration);
        final RadioButton rbLong = findViewById(R.id.activity_settings_rb_long_vibration);

        final RadioButton rbDefault = findViewById(R.id.activity_settings_rb_default);
        final RadioButton rbNotification = findViewById(R.id.activity_settings_rb_notification);
        final RadioButton rbChoose = findViewById(R.id.activity_settings_rb_sound_choose);

        final RadioButton rbExact = findViewById(R.id.activity_settings_rb_alarm_exact);
        final RadioButton rbInexact = findViewById(R.id.activity_settings_rb_alarm_inexact);

        final RadioButton rbResetSet = findViewById(R.id.activity_settings_rb_reset_time_set);
        final NumberPicker npTime = findViewById(R.id.activity_settings_np_reset_time);
        final RadioButton rbResetInfinite = findViewById(R.id.activity_settings_rb_reset_time_infinite);

        final RadioButton rbNotificationOn = findViewById(R.id.activity_settings_rb_notification_on);
        final RadioButton rbNotificationOff = findViewById(R.id.activity_settings_rb_notification_off);

        //These 2 lines allow for the SharedPreferences to be edited
        SharedPreferences settings = getSharedPreferences("setting", 0);
        SharedPreferences.Editor editor = settings.edit();
        // This checks for which alarm type radio button is checked and saves it in SharedPreferences
        if (rbExact.isChecked()) {
            editor.putString("alarmType", "exact");
        } else if (rbInexact.isChecked()) {
            editor.putString("alarmType", "inexact");
        }
        // This checks for which sound file radio button is checked and saves it in SharedPreferences
        if (rbDefault.isChecked()) {
            editor.putString("soundFile", "alarm");
        } else if (rbNotification.isChecked()) {
            editor.putString("soundFile", "notification");
        } else if (rbChoose.isChecked()) {
            editor.putString("soundFile", "choose");
            editor.putString("chooseFile", musicPath);
        }
        //This checks for which vibration radio button is checked and saves it in SharedPreferences
        if (rbShort.isChecked()) {
            editor.putString("vibrations", "short");
        } else if (rbMedium.isChecked()) {
            editor.putString("vibrations", "medium");
        } else if (rbLong.isChecked()) {
            editor.putString("vibrations", "long");
        } else if (rbNone.isChecked()) {
            editor.putString("vibrations", "none");
        }
        // This checks for which sound file radio button is checked and saves it in SharedPreferences
        if (rbResetSet.isChecked()) {
            long microSeconds;
            if (npTime.getValue() > 0) {
                microSeconds = npTime.getValue() * 1000;
            } else if (npTime.getValue() == 0) {
                microSeconds = npTime.getValue();
            } else {
                microSeconds = 20000;
            }
            editor.putLong("resetTime", microSeconds);
        } else if (rbResetInfinite.isChecked()) {
            editor.putLong("resetTime", 0);
        }
        // This checks for which sound file radio button is checked and saves it in SharedPreferences
        if (rbNotificationOn.isChecked()) {
            editor.putString("notifications", "on");

            if (settings.getBoolean("timerRunning", true)) {
                startNotification();
            }

        } else if (rbNotificationOff.isChecked()) {
            editor.putString("notifications", "off");
            if (settings.getBoolean("timerRunning", true)) {
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.cancelAll();
            }

        }
        editor.apply();
        Intent settingsIntent = new Intent(this, MainActivity.class);
        startActivity(settingsIntent);
        /*Toast settingsSavedToast = Toast.makeText(this, "Settings saved",
                Toast.LENGTH_SHORT);
        settingsSavedToast.show();*/
    }

    /**
     * This tries to save to SharedPreferences that the timer has stopped running when it is
     * destroyed
     */
    @Override
    public void onDestroy() {
        SharedPreferences settings = getSharedPreferences("setting", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("timerRunning", false);
        editor.apply();
        super.onDestroy();
    }

    private void startNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, 0);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("202020 App")
                .setContentText("Alarm is running")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_ALARM);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(SettingsActivity.this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, builder.build());
    }
}