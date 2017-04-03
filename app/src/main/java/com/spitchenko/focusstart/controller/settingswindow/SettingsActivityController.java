package com.spitchenko.focusstart.controller.settingswindow;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.userinterface.settingswindow.SettingsFragment;

/**
 * Date: 02.04.17
 * Time: 12:52
 *
 * @author anatoliy
 */
public final class SettingsActivityController {
    private AppCompatActivity activity;

    public SettingsActivityController(final AppCompatActivity activity) {
        this.activity = activity;
    }

    public void updateOnCreate(@Nullable final Bundle savedInstanceState) {
        activity.setContentView(R.layout.activity_settings);

        final Toolbar toolbar = (Toolbar) activity.findViewById(R.id.activity_settings_toolbar);
        activity.setSupportActionBar(toolbar);
        if (null != activity.getSupportActionBar()) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (null == savedInstanceState) {
            activity.getFragmentManager().beginTransaction().replace(R.id.app_bar_layout_container
                    , new SettingsFragment()).commit();
        }
    }
}
