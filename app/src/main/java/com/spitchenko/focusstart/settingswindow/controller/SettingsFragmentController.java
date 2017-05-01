package com.spitchenko.focusstart.settingswindow.controller;

import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.base.controller.AlarmController;
import com.spitchenko.focusstart.base.view.BaseActivity;

import lombok.NonNull;

/**
 * Date: 03.04.17
 * Time: 16:15
 *
 * @author anatoliy
 */
public class SettingsFragmentController {
    private PreferenceFragment fragment;

    public SettingsFragmentController(@NonNull final PreferenceFragment fragment) {
        this.fragment = fragment;
    }

    public void updateOnCreate() {
        fragment.setHasOptionsMenu(true);

        fragment.addPreferencesFromResource(R.xml.settings);

        final BaseActivity activity = (BaseActivity) fragment.getActivity();

        final AlarmController alarmController = new AlarmController(activity);

        final ListPreference timeList = (ListPreference) fragment.findPreference(activity.getResources()
                .getString(R.string.settings_fragment_notifications_list));

        alarmController.saveTimeSecondsToPreferences(Integer.parseInt(
                timeList.getValue()), activity);

        timeList.setSummary(timeList.getEntry());

        timeList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull final Preference preference
                    , @NonNull final Object newValue) {
                final int index = timeList.findIndexOfValue(newValue.toString());
                if (index != -1) {
                    alarmController.saveTimeSecondsToPreferences(Integer.parseInt(newValue
                                    .toString())
                            , activity);
                    alarmController.restartAlarm();
                    timeList.setSummary(timeList.getEntries()[index]);
                    timeList.setValueIndex(index);
                }
                return false;
            }
        });

        final Preference timeCheckBox = fragment.findPreference(fragment.getResources()
                .getString(R.string.settings_fragment_notification_checkbox));

        timeCheckBox.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull final Preference preference) {
                final SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(activity);
                alarmController.saveTimeSecondsToPreferences(Integer.parseInt(timeList.getValue())
                        , activity);
                if (!sharedPreferences.getBoolean(fragment.getActivity().getResources()
                        .getString(R.string.settings_fragment_notification_checkbox), false)) {
                    alarmController.stopAlarm();
                } else {
                    alarmController.startAlarm();
                }
                return true;
            }
        });

        final ListPreference themeList = (ListPreference) fragment.findPreference(fragment
                .getActivity().getResources().getString(R.string.settings_fragment_theme_list));

        if (null != themeList.getEntry()) {
            themeList.setSummary(themeList.getEntry());
            themeList.setValueIndex(findIndexOfEntry(themeList));
        } else {
            setDefaultThemeList(themeList);
        }

        themeList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull final Preference preference
                    , @NonNull final Object newValue) {

                final String theme = newValue.toString();

                final int index = themeList.findIndexOfValue(theme);
                if (index != -1) {
                    themeList.setSummary(themeList.getEntries()[index]);
                    themeList.setValueIndex(index);
                    activity.setTheme(theme);
                }
                return false;
            }
        });
    }

    private void initToolbar() {
        final android.support.v7.widget.Toolbar toolbar
                = (android.support.v7.widget.Toolbar) fragment.getActivity()
                .findViewById(R.id.activity_main_toolbar);

        toolbar.setTitle(fragment.getString(R.string.settings_activity_title));

        final AppCompatActivity activity = (AppCompatActivity) fragment.getActivity();
        activity.setSupportActionBar(toolbar);
        if (null != activity.getSupportActionBar()) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private int findIndexOfEntry(final ListPreference listPreference) {
        for (int i = 0; i < listPreference.getEntries().length; i++) {
            if (listPreference.getEntries()[i].toString().equals(listPreference.getEntry())) {
                return i;
            }
        }

        return -1;
    }

    private void setDefaultThemeList(final ListPreference themeList) {
        themeList.setSummary(fragment.getString(R.string.settings_fragment_theme_list_default));
        themeList.setValueIndex(0);
    }

    public void updateOnCreateView() {
        initToolbar();
    }

    public void updateOnCreateOptionsMenu(final Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(false);
    }
}
