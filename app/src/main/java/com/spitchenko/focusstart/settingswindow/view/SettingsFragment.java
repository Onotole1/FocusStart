package com.spitchenko.focusstart.settingswindow.view;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spitchenko.focusstart.base.view.BaseActivity;
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
    private final static String SETTINGS_FRAGMENT
            = "com.spitchenko.focusstart.settingswindow.view.SettingsFragment";

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

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container
            , @Nullable final Bundle savedInstanceState) {
        addObserver(settingsFragmentController);
        notifyOnCreateView();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void notifyOnCreateView() {
        for (int i = 0, size = observers.size(); i < size; i++) {
            final SettingsFragmentController settingsFragmentController = observers.get(i);
            settingsFragmentController.updateOnCreateView();
        }
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        notifyOnCreateOptionsMenu(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void notifyOnCreateOptionsMenu(final Menu menu) {
        for (int i = 0, size = observers.size(); i < size; i++) {
            final SettingsFragmentController settingsFragmentController = observers.get(i);
            settingsFragmentController.updateOnCreateOptionsMenu(menu);
        }
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

    public static String getSettingsFragmentKey() {
        return SETTINGS_FRAGMENT;
    }

    public static void start(final BaseActivity activity) {
        activity.setSettingsFragment();
    }
}
