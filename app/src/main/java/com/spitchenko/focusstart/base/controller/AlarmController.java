package com.spitchenko.focusstart.base.controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import lombok.NonNull;

/**
 * Date: 26.03.17
 * Time: 14:51
 *
 * @author anatoliy
 */
public final class AlarmController {
    private final static String ALARM_CONTROLLER = "com.spitchenko.focusstart.base.controller.AlarmController";
    private final static String SECONDS = ALARM_CONTROLLER + ".seconds";

    private Context context;

    public AlarmController(@NonNull final Context context) {
        this.context = context;
    }

    public void startAlarm() {
        final SharedPreferences preferences
                = context.getSharedPreferences(ALARM_CONTROLLER, Context.MODE_PRIVATE);
        final int seconds = preferences.getInt(SECONDS, 0);

        if (seconds > 0) {
            final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            final Intent intent = new Intent(context, RefreshBroadcastReceiver.class);
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), seconds * 1000
                    , pendingIntent);
        }
    }

    public void stopAlarm() {
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final Intent intent = new Intent(context, RefreshBroadcastReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    public void restartAlarm() {
        stopAlarm();
        startAlarm();
    }

    public void saveTimeSecondsToPreferences(final int seconds, @NonNull final Context context) {
        final SharedPreferences preferences
                = context.getSharedPreferences(ALARM_CONTROLLER, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SECONDS, seconds);
        editor.apply();
    }
}
