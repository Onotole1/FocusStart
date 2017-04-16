package com.spitchenko.focusstart.base.controller;

import android.content.Context;
import android.content.SharedPreferences;

import lombok.NonNull;

/**
 * Date: 11.04.17
 * Time: 11:31
 *
 * @author anatoliy
 */
public class NetworkDialogShowController {
    private final static String NETWORK_DIALOG_CONTROLLER = "com.spitchenko.focusstart.controller" +
            ".NetworkDialogShowController";

    private SharedPreferences preferences;

    public NetworkDialogShowController(@NonNull final Context context) {
        preferences = context.getSharedPreferences(NETWORK_DIALOG_CONTROLLER, Context.MODE_PRIVATE);
    }

    public void turnOnNetworkDialog() {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(NETWORK_DIALOG_CONTROLLER, true);
        editor.apply();
    }

    void turnOffNetworkDialog() {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(NETWORK_DIALOG_CONTROLLER, false);
        editor.apply();
    }

    public boolean isNetworkDialogShow() {
        return preferences.getBoolean(NETWORK_DIALOG_CONTROLLER, false);
    }
}
