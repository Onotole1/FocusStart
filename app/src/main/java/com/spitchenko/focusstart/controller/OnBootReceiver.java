package com.spitchenko.focusstart.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.spitchenko.focusstart.R;

import lombok.NonNull;

/**
 * Date: 26.03.17
 * Time: 16:38
 *
 * @author anatoliy
 */
public final class OnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                || intent.getAction().equals(Intent.ACTION_PACKAGE_FIRST_LAUNCH)
        || intent.getAction().equals(Intent.ACTION_PACKAGE_RESTARTED)) {
            final SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(context);
            if (sharedPreferences.getBoolean(context.getResources()
                    .getString(R.string.settings_fragment_notification_checkbox), false)) {
                final AlarmController alarmController = new AlarmController(context);
                alarmController.restartAlarm();
            }
        }
    }
}
