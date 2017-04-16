package com.spitchenko.focusstart.settingswindow.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.base.controller.BaseActivityController;
import com.spitchenko.focusstart.settingswindow.view.SettingsFragment;

import lombok.NonNull;

/**
 * Date: 02.04.17
 * Time: 12:52
 *
 * @author anatoliy
 */
public final class SettingsActivityController extends BaseActivityController {
    private final static String SETTINGS_ACTIVITY_CONTROLLER
            = "com.spitchenko.focusstart.settingswindow.controller.SettingsActivityController";
    private final static String LOCAL_THEME = SETTINGS_ACTIVITY_CONTROLLER + ".localTheme";
    private AppCompatActivity activity;

    public SettingsActivityController(@NonNull final AppCompatActivity activity) {
        this.activity = activity;
    }

    public void updateOnCreate(@Nullable final Bundle savedInstanceState) {
        setThemeFromPrefs(activity);
        activity.setContentView(R.layout.activity_settings);

        final Toolbar toolbar = (Toolbar) activity.findViewById(R.id.activity_settings_toolbar);
        activity.setSupportActionBar(toolbar);
        if (null != activity.getSupportActionBar()) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        activity.getSupportActionBar().setTitle(R.string.settings_activity_title);
        if (null == savedInstanceState) {
            activity.getFragmentManager().beginTransaction().replace(R.id.app_bar_layout_container
                    , new SettingsFragment()).commit();
        }
    }

    public void updateOnResume() {
        if (readLocalThemeIdFromPrefs() != readThemeIdOrNullFromPrefs(activity)) {
            activity.recreate();
        }
    }

    @Override
    protected void saveLocalThemeIdToPrefs(final int themeId) {
        final SharedPreferences preferences
                = activity.getSharedPreferences(LOCAL_THEME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(LOCAL_THEME, themeId);
        editor.apply();
    }

    @Override
    protected int readLocalThemeIdFromPrefs() {
        final SharedPreferences preferences
                = activity.getSharedPreferences(LOCAL_THEME, Context.MODE_PRIVATE);
        return preferences.getInt(LOCAL_THEME, 0);
    }
}
