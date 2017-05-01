package com.spitchenko.focusstart.base.controller;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.base.view.BaseActivity;
import com.spitchenko.focusstart.channelitemwindow.view.ChannelItemFragment;
import com.spitchenko.focusstart.channelwindow.controller.ChannelAddDialogFragment;
import com.spitchenko.focusstart.channelwindow.controller.RssChannelIntentService;
import com.spitchenko.focusstart.channelwindow.view.ChannelFragment;
import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.settingswindow.view.SettingsFragment;

import java.util.ArrayList;

import lombok.NonNull;

/**
 * Date: 28.04.17
 * Time: 21:25
 *
 * @author anatoliy
 */
public class MainController {
    private final static String MAIN_CONTROLLER
            = "com.spitchenko.focusstart.base.controller.MainController";
    private final static String THEME_PREFERENCES = MAIN_CONTROLLER + ".themePreferences";

    private final static String STYLE_TYPENAME = "style";
    private final static int BAD_VALUE = -1;

    private BaseActivity activity;

    public MainController(final BaseActivity activity) {
        this.activity = activity;
    }

    public void updateOnCreate(@Nullable final Bundle savedInstanceState) {
        setThemeActivity();
        activity.setContentView(R.layout.activity_main);

        if (null == savedInstanceState) {

            final String action = activity.getIntent().getAction();

            if (null != action && action.equals(BaseActivity
                    .getBaseActivityNotificationKey())) {
                setChannelFragment(ChannelFragment.getChannelFragmentNotificationKey());
            } else {
                setChannelFragment(null);
            }
        }
    }

    public void updateOnSetChannelItemFragment(final Channel channel) {
        final FragmentManager manager = activity.getFragmentManager();
        final FragmentTransaction fragmentTransaction = manager.beginTransaction();

        final ChannelItemFragment fragment = new ChannelItemFragment();
        final Bundle arguments = new Bundle();
        arguments.putParcelable(ChannelItemFragment.getChannelUrlKey(), channel);
        fragment.setArguments(arguments);

        fragmentTransaction.replace(R.id.activity_main_container, fragment
                , ChannelItemFragment.getChannelUrlKey());

        fragmentTransaction.addToBackStack(ChannelItemFragment.getChannelItemFragmentKey());

        fragmentTransaction.commit();
    }

    public void updateOnSupportNavigateUp() {
        final FragmentManager manager = activity.getFragmentManager();
        manager.popBackStackImmediate();
    }

    private void setChannelFragment(@Nullable final String action) {
        final FragmentManager manager = activity.getFragmentManager();
        final FragmentTransaction fragmentTransaction = manager.beginTransaction();

        final ChannelFragment channelFragment = new ChannelFragment();

        if (null != action) {
            final Bundle arguments = new Bundle();
            arguments.putString(ChannelFragment.getChannelFragmentNotificationKey(), action);
            channelFragment.setArguments(arguments);
        }

        fragmentTransaction.add(R.id.activity_main_container, channelFragment
                , ChannelFragment.getChannelFragmentKey());
        fragmentTransaction.commit();
    }

    public void updateOnSetSettingsFragment() {

        final FragmentManager manager = activity.getFragmentManager();
        final FragmentTransaction fragmentTransaction = manager.beginTransaction();

        final SettingsFragment fragment = new SettingsFragment();

        fragmentTransaction.replace(R.id.activity_main_container, fragment
                , SettingsFragment.getSettingsFragmentKey());

        fragmentTransaction.addToBackStack(SettingsFragment.getSettingsFragmentKey());

        fragmentTransaction.commit();
    }

    public void updateOnSetTheme(final String theme) {
        final int style = activity.getResources().getIdentifier(theme, STYLE_TYPENAME
                , activity.getPackageName());
        saveThemeIdToPrefs(style);

        final Intent activityIntent = activity.getIntent();
        activity.finish();
        activity.startActivity(activityIntent);
    }

    private void saveThemeIdToPrefs(final int themeId) {
        final SharedPreferences preferences
                = activity.getSharedPreferences(THEME_PREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = preferences.edit();
        edit.putInt(THEME_PREFERENCES, themeId);
        edit.apply();
    }

    private void setThemeActivity() {
        final SharedPreferences preferences
                = activity.getSharedPreferences(THEME_PREFERENCES, Context.MODE_PRIVATE);
        final int themeId = preferences.getInt(THEME_PREFERENCES, BAD_VALUE);
        if (BAD_VALUE != themeId) {
            activity.setTheme(themeId);
        }
    }

    public void updateOnNewIntent(final Intent intent) {
        if (intent.getAction().equals(BaseActivity.getBaseActivityNotificationKey())) {
            final ArrayList<String> channelsUrls
                    = intent.getStringArrayListExtra(BaseActivity.getBaseActivityNotificationKey());

            checkUpdates(channelsUrls);
        }
    }

    private void checkUpdates(@NonNull final ArrayList<String> receivedChannels) {
        final SharedPreferences preferences
                = RssChannelIntentService.getReadMessagesPreferences(activity);
        for (int i = 0, size = receivedChannels.size(); i < size; i++) {
            final String channelUrl = receivedChannels.get(i);
            final String message = preferences.getString(channelUrl, null);
            if (null != message) {
                showRefreshDialog(channelUrl, message);
            }
        }
    }

    private void showRefreshDialog(@NonNull final String url, @NonNull final String message) {
        final SharedPreferences preferences
                = RssChannelIntentService.getReadMessagesPreferences(activity);
        final SharedPreferences.Editor edit = preferences.edit();
        edit.putString(url, null);
        edit.apply();

        final ChannelRefreshDialog channelRefreshDialog = new ChannelRefreshDialog();
        final Bundle refreshBundle = new Bundle();
        refreshBundle.putString(ChannelRefreshDialog.getChannelUrlKey(), url);
        refreshBundle.putString(ChannelRefreshDialog.getMessageKey(), message);
        channelRefreshDialog.setArguments(refreshBundle);
        final android.app.FragmentTransaction fragmentTransaction
                = activity.getFragmentManager().beginTransaction();
        fragmentTransaction.add(channelRefreshDialog, ChannelAddDialogFragment.getDialogFragmentTag());
        fragmentTransaction.commitAllowingStateLoss();
    }
}
