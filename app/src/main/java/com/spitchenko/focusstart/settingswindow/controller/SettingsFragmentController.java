package com.spitchenko.focusstart.settingswindow.controller;

import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.base.controller.AlarmController;

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
        fragment.addPreferencesFromResource(R.xml.settings);
        final AlarmController alarmController = new AlarmController(fragment.getActivity());

        final ListPreference timeList = (ListPreference) fragment.findPreference(fragment.getActivity().getResources()
                .getString(R.string.settings_fragment_notifications_list));

        alarmController.saveTimeSecondsToPreferences(Integer.parseInt(
                timeList.getValue()), fragment.getActivity());

        timeList.setSummary(timeList.getEntry());

        timeList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull final Preference preference
                    , @NonNull final Object newValue) {
                final int index = timeList.findIndexOfValue(newValue.toString());
                if (index != -1) {
                    alarmController.saveTimeSecondsToPreferences(Integer.parseInt(newValue.toString())
                            , fragment.getActivity());
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
                        .getDefaultSharedPreferences(fragment.getActivity());
                alarmController.saveTimeSecondsToPreferences(Integer.parseInt(timeList.getValue())
                        , fragment.getActivity());
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
        themeList.setSummary(themeList.getEntry());
        themeList.setValueIndex(findIndexOfEntry(themeList));

        themeList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull final Preference preference
                    , @NonNull final Object newValue) {
                final int index = themeList.findIndexOfValue(newValue.toString());
                if (index != -1) {
                    themeList.setSummary(themeList.getEntries()[index]);
                    themeList.setValueIndex(index);
                    fragment.getActivity().recreate();
                }
                return false;
            }
        });
    }

    private int findIndexOfEntry(final ListPreference listPreference) {
        for (int i = 0; i < listPreference.getEntries().length; i++) {
            if (listPreference.getEntries()[i].toString().equals(listPreference.getEntry())) {
                return i;
            }
        }

        return -1;
    }
}
