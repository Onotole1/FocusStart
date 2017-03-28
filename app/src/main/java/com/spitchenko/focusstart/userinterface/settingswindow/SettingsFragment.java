package com.spitchenko.focusstart.userinterface.settingswindow;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.TimePicker;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.controller.AlarmController;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Date: 21.03.17
 * Time: 15:06
 *
 * @author anatoliy
 */
public final class SettingsFragment extends PreferenceFragment {
	private final static String SETTINGS_FRAGMENT = "com.spitchenko.focusstart.userinterface.settingswindow.SettingsFragment";
	private final static String SHOW_DIALOG = SETTINGS_FRAGMENT + ".settingsFragment";
	private TimePickerDialog timePickerDialog;
	private int myHour;
	private int myMinute;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
        final AlarmController alarmController = new AlarmController();
		final Preference timeButton = findPreference(getActivity().getResources().getString(R.string.notification_button));
        myHour = alarmController.readHour(getActivity());
        myMinute = alarmController.readMinute(getActivity());
		timeButton.setSummary(formatTimeSummary(myHour, myMinute));
		final Preference timeCheckBox = findPreference(getActivity().getResources().getString(R.string.notification_checkbox));

		final TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(final TimePicker view, final int hourOfDay, final int minute) {
				myHour = hourOfDay;
				myMinute = minute;
                alarmController.saveTimeToPreferences(myHour, myMinute, getActivity());
				timeButton.setSummary(formatTimeSummary(myHour, myMinute));
				if (timeCheckBox.isEnabled()) {
                    alarmController.saveTimeToPreferences(myHour, myMinute, getActivity());
                    alarmController.restartAlarm(getActivity());
				}
			}
		};

		timePickerDialog = new TimePickerDialog(getActivity(), timeSetListener, myHour, myMinute, true);

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
				final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
				if (!sharedPreferences.getBoolean(getActivity().getResources()
						.getString(R.string.notification_checkbox), false)) {
					alarmController.stopAlarm(getActivity());
				} else {
                    alarmController.startAlarm(getActivity());
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

	@Override
	public void onViewStateRestored(final Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (null != savedInstanceState) {
			final Bundle dialogState = savedInstanceState.getBundle(SETTINGS_FRAGMENT);
			if (null != dialogState) {
				timePickerDialog.onRestoreInstanceState(dialogState);
			}
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		if (timePickerDialog.isShowing()) {
			outState.putBoolean(SHOW_DIALOG, true);
		}
		outState.putParcelable(SETTINGS_FRAGMENT, timePickerDialog.onSaveInstanceState());
		timePickerDialog.dismiss();
	}

	private String formatTimeSummary(final int hour, final int minute) {
		final Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);

		final java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("HH:mm", Locale.ENGLISH);
		return simpleDateFormat.format(calendar.getTime());
	}
}
