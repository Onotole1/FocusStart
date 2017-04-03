package com.spitchenko.focusstart.userinterface.settingswindow;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.spitchenko.focusstart.controller.settingswindow.SettingsFragmentController;

import java.util.ArrayList;

/**
 * Date: 21.03.17
 * Time: 15:06
 *
 * @author anatoliy
 */
public final class SettingsFragment extends PreferenceFragment {
    private ArrayList<SettingsFragmentController> observers = new ArrayList<>();
    private SettingsFragmentController settingsFragmentController = new SettingsFragmentController(this);

	@Override
	public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addObserver(settingsFragmentController);
        notifyOnCreate(savedInstanceState);
	}

	@Override
	public void onViewStateRestored(final Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
        notifyOnViewStateRestored(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
        notifyOnSaveInstanceState(outState);
        removeObserver(settingsFragmentController);
        super.onSaveInstanceState(outState);
    }



	private void notifyOnCreate(final Bundle savedInstanceState) {
        for (final SettingsFragmentController settingsFragmentController:observers) {
            settingsFragmentController.updateOnCreate(savedInstanceState);
        }
    }

    private void notifyOnViewStateRestored(final Bundle savedInstanceState) {
        for (final SettingsFragmentController settingsFragmentController:observers) {
            settingsFragmentController.updateOnViewStateRestored(savedInstanceState);
        }
    }

    private void notifyOnSaveInstanceState(final Bundle outState) {
        for (final SettingsFragmentController settingsFragmentController:observers) {
            settingsFragmentController.updateOnSaveInstanceState(outState);
        }
    }

    private void addObserver(final SettingsFragmentController observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    private void removeObserver(final SettingsFragmentController observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }
}
