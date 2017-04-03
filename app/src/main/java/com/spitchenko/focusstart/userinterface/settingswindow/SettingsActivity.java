package com.spitchenko.focusstart.userinterface.settingswindow;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.spitchenko.focusstart.controller.settingswindow.SettingsActivityController;

import java.util.ArrayList;

/**
 * Date: 23.03.17
 * Time: 9:29
 *
 * @author anatoliy
 */
public final class SettingsActivity extends AppCompatActivity {
    ArrayList<SettingsActivityController> observers = new ArrayList<>();
    SettingsActivityController settingsActivityController = new SettingsActivityController(this);

	@Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        addObserver(settingsActivityController);
		super.onCreate(savedInstanceState);
        notifyOnCreate(savedInstanceState);
        removeObserver(settingsActivityController);
	}

    @Override
	public boolean onSupportNavigateUp() {
		onBackPressed();
		return true;
	}

	private void notifyOnCreate(final Bundle savedInstanceState) {
        for (final SettingsActivityController controller: observers) {
            controller.updateOnCreate(savedInstanceState);
        }
    }

	private void addObserver(final SettingsActivityController observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    private void removeObserver(final SettingsActivityController observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }
}
