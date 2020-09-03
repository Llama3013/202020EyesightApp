package com.test.a202020app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class AlarmActivity extends AppCompatActivity {
    private SharedPreferences settings;
    private Ringtone alarm;
    private Vibrator vibration;
    private Uri alarmSound;
    private CountDownTimer twentyTimer;

    /**
     * This function will need too be changed soon for the ability to play different sounds for the
     * alarm
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("debug", "AlarmActivity: onCreate");
        //This line will turn the screen on
        setContentView(R.layout.activity_alarm);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        super.onCreate(savedInstanceState);
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "202020app: powerManager");
        wl.acquire(30000);
        //setTurnScreenOn(true);
        //setShowWhenLocked(true);
        //These two lines will load soundFile setting and save it to the soundFile variable
        settings = this.getSharedPreferences("setting", 0);
        String soundFile = settings.getString("soundFile", "alarm");
        long microSeconds = settings.getLong("resetTime", 20000);

        /* This finds which soundFile should be played and then sets it to the alarmSound Uri
           variable */
        switch (Objects.requireNonNull(soundFile)) {
            case ("alarm"):
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                Log.d("debug", "alarm" + alarmSound.toString());
                break;
            case ("notification"):
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Log.d("debug", "alarm notification" + alarmSound.toString());
                break;
            case ("choose"):
                String chooseFile = settings.getString("chooseFile", "not working");
                alarmSound = Uri.parse(chooseFile);
                break;
            default:
                Log.d("debug", "alarm sound was not set");
                break;
        }
        if (alarmSound != null) {
            // These two lines play the alarm sound
            alarm = RingtoneManager.getRingtone(this, alarmSound);
            alarm.play();
        } else {
            Log.d("debug", "alarm sound was not played");
        }
        startVibrate();

        if (microSeconds == 0) {
            microSeconds = 2147483647;
        }
        /* This CountDownTimer counts to 20 seconds and then resets then calls the coolDownReset
           function unless stopped by the user input*/
        twentyTimer = new CountDownTimer(microSeconds, 1000) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                coolDownReset();
            }
        }.start();
    }

    /**
     * When the user taps on button reset then it will call the coolDownReset() function
     */
    public void btnReset(View view) {
        coolDownReset();
    }

    /**
     * This tries to save to SharedPreferences that the timer has stopped running when it is
     * destroyed
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

    /**
     * This will save to SharedPreferences that the timer is running. It stop the twenty second
     * timer, the sound and vibrations. It will get the CountdownTimer from TimerService to cancel
     * it then start it again. It will show a toast saying Timer reset and it will start the main
     * activity.
     */
    private void coolDownReset() {
        Log.d("debug", "reset");
        twentyTimer.cancel();
        if (alarm != null) {
            alarm.stop();
        }
        if (vibration != null) {
            vibration.cancel();
        }
        /*Toast myToast = Toast.makeText(this, "Timer reset", Toast.LENGTH_SHORT);
        myToast.show();*/
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("newAlarm", true);
        this.startActivity(intent);
        this.finish();
    }

    /**
     * This will create a service intent to stop the foreground service, stops vibrations, stops
     * alarm sound and starts the main activity
     */
    public void stop(View view) {
        Log.d("debug", "stop");
        twentyTimer.cancel();
        if (alarm != null) {
            alarm.stop();
        }
        if (vibration != null) {
            vibration.cancel();
        }
        /*Toast myToast = Toast.makeText(this, "Timer stop", Toast.LENGTH_SHORT);
        myToast.show();*/
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("newAlarm", false);
        this.startActivity(intent);
        this.finish();
    }

    /**
     * This gets the vibrations setting and starts the vibration if turned on and if phone
     * is capable of vibrating
     */
    private void startVibrate() {
        //This line loads the vibration setting
        String vibrationPattern = settings.getString("vibrations", "none");
        // This switch statement finds the correct vibration pattern
        long[] pattern;
        switch (Objects.requireNonNull(vibrationPattern)) {
            case "short":
                pattern = new long[]{0, 250, 100};
                break;
            case "medium":
                pattern = new long[]{0, 500, 200};
                break;
            case "long":
                pattern = new long[]{0, 1000, 300};
                break;
            case "none":
                pattern = null;
                break;
            default:
                pattern = null;
                final Toast vibeSettingErrorToast = Toast.makeText(this,
                        "vibrate setting wasn't found", Toast.LENGTH_SHORT);
                vibeSettingErrorToast.show();
                break;
        }
        if (pattern != null) {
            vibration = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibration.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //This line start the vibrations
                    vibration.vibrate(VibrationEffect.createWaveform(pattern, 0));
                } else {
                    vibration.vibrate(pattern, 0);
                }
            }
        }
    }

}