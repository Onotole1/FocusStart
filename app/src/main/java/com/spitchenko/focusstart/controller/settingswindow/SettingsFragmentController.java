package com.spitchenko.focusstart.controller.settingswindow;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.TimePicker;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.controller.AlarmController;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Date: 03.04.17
 * Time: 16:15
 *
 * @author anatoliy
 */
public class SettingsFragmentController {
    private final static String SETTINGS_FRAGMENT_CONTROLLER = "com.spitchenko.focusstart" +
            ".controller.settingswindow.SettingsFragmentController";
    private final static String SHOW_DIALOG = SETTINGS_FRAGMENT_CONTROLLER + ".settingsFragment";
    private PreferenceFragment fragment;
    private TimePickerDialog timePickerDialog;
    private int myHour;
    private int myMinute;

    public SettingsFragmentController(final PreferenceFragment fragment) {
        this.fragment = fragment;
    }

    public void updateOnCreate(final Bundle savedInstanceState) {
        fragment.addPreferencesFromResource(R.xml.settings);
        final AlarmController alarmController = new AlarmController(fragment.getActivity());
        final Preference timeButton
                = fragment.findPreference(fragment.getActivity().getResources()
                .getString(R.string.notification_button));
        myHour = alarmController.readHour(fragment.getActivity());
        myMinute = alarmController.readMinute(fragment.getActivity());
        timeButton.setSummary(formatTimeSummary(myHour, myMinute));
        final Preference timeCheckBox = fragment.findPreference(fragment.getActivity()
                .getResources().getString(R.string.notification_checkbox));

        final TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
                myHour = hourOfDay;
                myMinute = minute;
                alarmController.saveTimeToPreferences(myHour, myMinute, fragment.getActivity());
                timeButton.setSummary(formatTimeSummary(myHour, myMinute));
                if (timeCheckBox.isEnabled()) {
                    alarmController.saveTimeToPreferences(myHour, myMinute, fragment.getActivity());
                    alarmController.restartAlarm();
                }
            }
        };

        timePickerDialog = new TimePickerDialog(fragment.getActivity(), timeSetListener, myHour
                , myMinute, true);

        timeButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                timePickerDialog.show();
                return true;
            }
        });

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

        if (null != savedInstanceState) {
            if (savedInstanceState.getBoolean(SHOW_DIALOG)) {
                timePickerDialog.show();
            }
        }
    }

    private String formatTimeSummary(final int hour, final int minute) {
        final Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        final java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("HH:mm", Locale.ENGLISH);
        return simpleDateFormat.format(calendar.getTime());
    }

    public void updateOnViewStateRestored(@Nullable final Bundle state) {
        if (null != state) {
            final Bundle stateDialog = state.getParcelable(SETTINGS_FRAGMENT_CONTROLLER);
            if (null != stateDialog) {
                timePickerDialog.onRestoreInstanceState(stateDialog);
            }
        }
    }

    public void updateOnSaveInstanceState(final Bundle state) {
        if (timePickerDialog.isShowing()) {
            state.putBoolean(SHOW_DIALOG, true);
        }
        state.putParcelable(SETTINGS_FRAGMENT_CONTROLLER, timePickerDialog.onSaveInstanceState());
        timePickerDialog.dismiss();
    }
}
