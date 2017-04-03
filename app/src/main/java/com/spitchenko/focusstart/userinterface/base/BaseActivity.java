package com.spitchenko.focusstart.userinterface.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.controller.ActivityAndBroadcastObserver;
import com.spitchenko.focusstart.userinterface.settingswindow.SettingsActivity;

import java.util.ArrayList;

import lombok.NonNull;

/**
 * Date: 23.03.17
 * Time: 14:17
 *
 * @author anatoliy
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected ArrayList<ActivityAndBroadcastObserver> observers = new ArrayList<>();

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notifyOnCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        notifyOnPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyOnResume();
    }

    @Override
	public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
		getMenuInflater().inflate(R.menu.channel_menu, menu);
		return true;
	}

	@Override
	public boolean onSupportNavigateUp() {
		onBackPressed();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				showSettingsFragment();
		}
		return super.onOptionsItemSelected(item);
	}

	private void showSettingsFragment() {
		final Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        notifyOnRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        notifyOnSavedInstanceState(outState);
    }

    protected void addObserver(final ActivityAndBroadcastObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    protected void removeObserver(final ActivityAndBroadcastObserver observer) {
        if (!observers.isEmpty()) {
            observers.remove(observer);
        }
    }

    private void notifyOnCreate(final Bundle savedInstanceState) {
        for (final ActivityAndBroadcastObserver observer:observers) {
            observer.updateOnCreate(savedInstanceState);
        }
    }

    private void notifyOnResume() {
        for (final ActivityAndBroadcastObserver observer:observers) {
            observer.updateOnResume();
        }
    }

    private void notifyOnSavedInstanceState(final Bundle outState) {
        for (final ActivityAndBroadcastObserver observer:observers) {
            observer.updateOnSavedInstanceState(outState);
        }
    }

    private void notifyOnRestoreInstanceState(final Bundle savedInstanceState) {
        for (final ActivityAndBroadcastObserver observer:observers) {
            observer.updateOnRestoreInstanceState(savedInstanceState);
        }
    }

    private void notifyOnPause() {
        for (final ActivityAndBroadcastObserver observer:observers) {
            observer.updateOnPause();
        }
    }
}
