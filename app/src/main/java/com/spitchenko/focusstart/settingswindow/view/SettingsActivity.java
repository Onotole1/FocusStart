package com.spitchenko.focusstart.settingswindow.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.spitchenko.focusstart.settingswindow.controller.SettingsActivityController;

import java.util.ArrayList;

import lombok.NonNull;

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
    protected final void onCreate(@Nullable final Bundle savedInstanceState) {
        addObserver(settingsActivityController);
		super.onCreate(savedInstanceState);
        notifyOnCreate(savedInstanceState);
	}

    @Override
    protected final void onResume() {
        super.onResume();
        notifyOnResume();
        removeObserver(settingsActivityController);
    }

    @Override
	public final boolean onSupportNavigateUp() {
		onBackPressed();
		return true;
	}

	private void notifyOnCreate(@Nullable final Bundle savedInstanceState) {
        for (final SettingsActivityController controller: observers) {
            controller.updateOnCreate(savedInstanceState);
        }
    }

    private void notifyOnResume() {
        for (final SettingsActivityController controller: observers) {
            controller.updateOnResume();
        }
    }

	private void addObserver(@NonNull final SettingsActivityController observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    private void removeObserver(@NonNull final SettingsActivityController observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }
}
