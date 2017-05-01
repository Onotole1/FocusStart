package com.spitchenko.focusstart.base.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.base.controller.MainController;
import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.settingswindow.view.SettingsFragment;

import java.util.ArrayList;

import lombok.NonNull;

/**
 * Date: 23.03.17
 * Time: 14:17
 *
 * @author anatoliy
 */
public class BaseActivity extends AppCompatActivity {
    private final static String BASE_ACTIVITY = "com.spitchenko.focusstart.base.view.BaseActivity";
    private final static String BASE_ACTIVITY_NOTIFICATION
            = BASE_ACTIVITY + ".baseActivityNotification";

    private final static int OBSERVERS_SIZE = 1;
    private ArrayList<MainController> observers = new ArrayList<>(OBSERVERS_SIZE);
    private MainController mainController = new MainController(this);

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        addObserver(mainController);
        super.onCreate(savedInstanceState);
        notifyOnCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            removeObserver(mainController);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.channel_menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        addObserver(mainController);
        super.onResume();
    }

    @Override
	public boolean onSupportNavigateUp() {
		notifyOnSupportNavigateUp();
		return true;
	}

    private void notifyOnSupportNavigateUp() {
        for (int i = 0, size = observers.size(); i < size; i++) {
            final MainController observer = observers.get(i);
            observer.updateOnSupportNavigateUp();
        }
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
        SettingsFragment.start(this);
	}

    private void addObserver(@NonNull final MainController observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    private void removeObserver(@NonNull final MainController observer) {
        if (!observers.isEmpty()) {
            observers.remove(observer);
        }
    }

    private void notifyOnCreate(@Nullable final Bundle savedInstanceState) {
        for (int i = 0, size = observers.size(); i < size; i++) {
            final MainController observer = observers.get(i);
            observer.updateOnCreate(savedInstanceState);
        }
    }

    public static void start(final Context context, @NonNull final ArrayList<String> channelUrls) {
        final Intent intent = new Intent(context, BaseActivity.class);
        intent.putStringArrayListExtra(BASE_ACTIVITY_NOTIFICATION, channelUrls);
        intent.setAction(BASE_ACTIVITY_NOTIFICATION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        notifyOnNewIntent(intent);
    }

    private void notifyOnNewIntent(final Intent intent) {
        for (int i = 0, size = observers.size(); i < size; i++) {
            final MainController observer = observers.get(i);
            observer.updateOnNewIntent(intent);
        }
    }

    public void setChannelItemFragment(final Channel channel) {
        notifyOnSetChannelItemFragment(channel);
    }

    private void notifyOnSetChannelItemFragment(final Channel channel) {
        for (int i = 0, size = observers.size(); i < size; i++) {
            final MainController observer = observers.get(i);
            observer.updateOnSetChannelItemFragment(channel);
        }
    }

    public void setSettingsFragment() {
        for (int i = 0, size = observers.size(); i < size; i++) {
            final MainController observer = observers.get(i);
            observer.updateOnSetSettingsFragment();
        }
    }

    public void setTheme(final String theme) {
        for (int i = 0, size = observers.size(); i < size; i++) {
            final MainController observer = observers.get(i);
            observer.updateOnSetTheme(theme);
        }
    }

    public static String getBaseActivityNotificationKey() {
        return BASE_ACTIVITY_NOTIFICATION;
    }
}
