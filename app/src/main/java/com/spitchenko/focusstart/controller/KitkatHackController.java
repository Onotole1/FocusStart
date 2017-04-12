package com.spitchenko.focusstart.controller;

import android.content.Context;
import android.content.SharedPreferences;

import lombok.NonNull;

/**
 * Date: 11.04.17
 * Time: 23:08
 *
 * @author anatoliy
 */
public class KitkatHackController {
    private final static String KITKAT_HACK_CONTROLLER
            = "com.spitchenko.focusstart.controller.kitkatController";
    private final static String ON_PAUSE_SEQUENCE = KITKAT_HACK_CONTROLLER + ".onPauseSequence";
    private SharedPreferences preferences;

    public KitkatHackController(@NonNull final Context context) {
        preferences = context.getSharedPreferences(KITKAT_HACK_CONTROLLER, Context.MODE_PRIVATE);
    }

    public void turnOnHackController() {
        setNullOnPauseSequence();
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KITKAT_HACK_CONTROLLER, true);
        editor.apply();
    }

    public void turnOffHackController() {
        setNullOnPauseSequence();
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KITKAT_HACK_CONTROLLER, false);
        editor.apply();
    }

    public boolean isHack() {
        return preferences.getBoolean(KITKAT_HACK_CONTROLLER, false);
    }

    public void setNullOnPauseSequence() {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(ON_PAUSE_SEQUENCE, 0);
        editor.apply();
    }

    public void onPauseSequenceIncrement() {
        final int sequence = preferences.getInt(ON_PAUSE_SEQUENCE, 0);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(ON_PAUSE_SEQUENCE, sequence + 1);
        editor.apply();
    }

    public int getOnPauseSequence() {
        return preferences.getInt(ON_PAUSE_SEQUENCE, 0);
    }
}
