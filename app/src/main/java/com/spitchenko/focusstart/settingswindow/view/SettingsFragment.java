package com.spitchenko.focusstart.settingswindow.view;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.spitchenko.focusstart.settingswindow.controller.SettingsFragmentController;

import java.util.ArrayList;

import lombok.NonNull;

/**
 * Date: 21.03.17
 * Time: 15:06
 *
 * @author anatoliy
 */
public final class SettingsFragment extends PreferenceFragment {
    private ArrayList<SettingsFragmentController> observers = new ArrayList<>();
    private SettingsFragmentController settingsFragmentController
            = new SettingsFragmentController(this);

	@Override
	public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addObserver(settingsFragmentController);
        notifyOnCreate();
	}

	@Override
	public void onSaveInstanceState(@Nullable final Bundle outState) {
        removeObserver(settingsFragmentController);
        super.onSaveInstanceState(outState);
    }



	private void notifyOnCreate() {
        for (int i = 0, size = observers.size(); i < size; i++) {
            final SettingsFragmentController settingsFragmentController = observers.get(i);
            settingsFragmentController.updateOnCreate();
        }
    }

    private void addObserver(@NonNull final SettingsFragmentController observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    private void removeObserver(@NonNull final SettingsFragmentController observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }
}
