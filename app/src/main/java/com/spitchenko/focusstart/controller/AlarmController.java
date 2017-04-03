package com.spitchenko.focusstart.controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Date: 26.03.17
 * Time: 14:51
 *
 * @author anatoliy
 */
public final class AlarmController {
    private final static String ALARM_CONTROLLER = "com.spitchenko.focusstart.controller.AlarmController";
    private final static String HOURS = ALARM_CONTROLLER + ".hours";
    private final static String MINUTES = ALARM_CONTROLLER + ".minutes";

    private Context context;

    public AlarmController(final Context context) {
        this.context = context;
    }

    public void startAlarm() {
        final SharedPreferences preferences
                = context.getSharedPreferences(ALARM_CONTROLLER, Context.MODE_PRIVATE);
        final int hour = preferences.getInt(HOURS, 0);
        final int minute = preferences.getInt(MINUTES, 0);

        if (minute > 0) {
            final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            final Intent intent = new Intent(context, RefreshBroadcastReceiver.class);
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), intervalMillis(hour, minute), pendingIntent);
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

    public void saveTimeToPreferences(final int hours, final int minutes, final Context context) {
        final SharedPreferences preferences
                = context.getSharedPreferences(ALARM_CONTROLLER, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(HOURS, hours);
        editor.putInt(MINUTES, minutes);
        editor.apply();
    }

    private long intervalMillis(final int hour, final int minute) {
        final long hourMillis = hour * 60 * 60 * 1000;
        final long minuteMillis = minute * 60 * 1000;
        return hourMillis + minuteMillis;
    }

    public int readHour(final Context context) {
        final SharedPreferences preferences
                = context.getSharedPreferences(ALARM_CONTROLLER, Context.MODE_PRIVATE);
        return preferences.getInt(HOURS,0);
    }

    public int readMinute(final Context context) {
        final SharedPreferences preferences
                = context.getSharedPreferences(ALARM_CONTROLLER, Context.MODE_PRIVATE);
        return preferences.getInt(MINUTES,0);
    }
}
