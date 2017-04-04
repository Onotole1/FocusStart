package com.spitchenko.focusstart.controller.settingswindow;

import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.controller.AlarmController;

/**
 * Date: 03.04.17
 * Time: 16:15
 *
 * @author anatoliy
 */
public class SettingsFragmentController {
    private PreferenceFragment fragment;

    public SettingsFragmentController(final PreferenceFragment fragment) {
        this.fragment = fragment;
    }

    public void updateOnCreate() {
        fragment.addPreferencesFromResource(R.xml.settings);
        final AlarmController alarmController = new AlarmController(fragment.getActivity());

        final ListPreference timeList
                = (ListPreference) fragment.findPreference(fragment.getActivity().getResources()
                .getString(R.string.notifications_list));

        alarmController.saveTimeSecondsToPreferences(Integer.parseInt(
                timeList.getValue()), fragment.getActivity());

        timeList.setSummary(timeList.getEntry());

        timeList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                final int index = timeList.findIndexOfValue(newValue.toString());
                if (index != -1) {
                    alarmController.saveTimeSecondsToPreferences(Integer.parseInt(
                            timeList.getEntryValues()[index].toString()), fragment.getActivity());
                    alarmController.restartAlarm();
                    timeList.setSummary(timeList.getEntries()[index]);
                    timeList.setValueIndex(index);
                }
                return false;
            }
        });

        final Preference timeCheckBox = fragment.findPreference(fragment.getActivity()
                .getResources().getString(R.string.notification_checkbox));

        timeCheckBox.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                final SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(fragment.getActivity());
                if (!sharedPreferences.getBoolean(fragment.getActivity().getResources()
                        .getString(R.string.notification_checkbox), false)) {
                    alarmController.stopAlarm();
                } else {
                    alarmController.startAlarm();
                }
                return true;
            }
        });
    }
}
