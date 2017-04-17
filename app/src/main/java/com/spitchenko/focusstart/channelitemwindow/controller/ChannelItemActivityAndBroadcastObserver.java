package com.spitchenko.focusstart.channelitemwindow.controller;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.base.controller.BaseActivityController;
import com.spitchenko.focusstart.base.controller.ChannelRecyclerEmptyAdapter;
import com.spitchenko.focusstart.base.controller.NetworkDialogShowController;
import com.spitchenko.focusstart.base.controller.NoInternetDialog;
import com.spitchenko.focusstart.base.controller.UpdateController;
import com.spitchenko.focusstart.channelwindow.controller.ChannelAddDialogFragment;
import com.spitchenko.focusstart.model.ChannelItem;
import com.spitchenko.focusstart.observer.ActivityAndBroadcastObserver;

import java.util.ArrayList;

import lombok.NonNull;

/**
 * Date: 01.04.17
 * Time: 14:01
 *
 * @author anatoliy
 */
public final class ChannelItemActivityAndBroadcastObserver extends BaseActivityController
        implements ActivityAndBroadcastObserver {
    private final static String CHANNEL_ITEM_ACTIVITY_OBSERVER
            = "com.spitchenko.focusstart.controller.channel_item_window " +
            ".ChannelItemActivityAndBroadcastObserver";
    private final static String CHANNEL_ITEM_ACTIVITY_PREFS_URL
            = CHANNEL_ITEM_ACTIVITY_OBSERVER + ".sharedUrl";
    private final static String RECYCLER_STATE = CHANNEL_ITEM_ACTIVITY_OBSERVER + ".recyclerState";
    private final static String SWIPE_LAYOUT_STATE = CHANNEL_ITEM_ACTIVITY_OBSERVER + ".swipeState";
    private final static String LOCAL_THEME = CHANNEL_ITEM_ACTIVITY_OBSERVER + ".localTheme";

    private AppCompatActivity activity;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String channelUrl;
    private Parcelable recyclerState;
    private LocalBroadcastManager localBroadcastManager;
    private ChannelItemBroadcastReceiver channelItemBroadcastReceiver;
    private UpdateController updateController;
    private NetworkDialogShowController networkDialogShowController;


    public ChannelItemActivityAndBroadcastObserver(final AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override
    public void updateOnCreate(@Nullable final Bundle savedInstanceState) {
        setThemeFromPrefs(activity);
        activity.setContentView(R.layout.activity_channel_item);

        final Toolbar toolbar = (Toolbar) activity.findViewById(R.id.activity_channel_item_toolbar);
        activity.setSupportActionBar(toolbar);
        if (null != activity.getSupportActionBar()) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        swipeRefreshLayout
                = (SwipeRefreshLayout) activity
                .findViewById(R.id.activity_channel_item_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RssChannelItemIntentService.start(null, channelUrl
                        , RssChannelItemIntentService.getRefreshChannelItemsKey(), activity);
            }
        });

        recyclerView = (RecyclerView) activity
                .findViewById(R.id.activity_channel_item_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(new ChannelRecyclerEmptyAdapter());

        if (null == savedInstanceState) {
            readUrlFromIntent();
        }

        channelUrl = readUrlFromPreferences();

        updateController = new UpdateController(activity);
        updateController.turnOnUpdate();

        networkDialogShowController = new NetworkDialogShowController(activity);
    }

    @Override
    public void updateOnResume() {
        if (readLocalThemeIdFromPrefs() != readThemeIdOrNullFromPrefs(activity)) {
            final Intent activityStarter = activity.getIntent();
            activity.finish();
            activity.startActivity(activityStarter);
            return;
        }

        subscribe();

        if (updateController.isUpdate()) {
            RssChannelItemIntentService.start(null, channelUrl
                    , RssChannelItemIntentService.getReadChannelsKey(), activity);

            updateController.turnOffUpdate();
        }
    }

    private void subscribe() {
        localBroadcastManager = LocalBroadcastManager.getInstance(activity);
        channelItemBroadcastReceiver = new ChannelItemBroadcastReceiver();
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ChannelItemBroadcastReceiver.getReceiveChannelItemsKey());
        intentFilter.addAction(ChannelItemBroadcastReceiver.getNoInternetAction());
        intentFilter.addAction(ChannelItemBroadcastReceiver.getChannelItemsRefresh());
        localBroadcastManager.registerReceiver(channelItemBroadcastReceiver, intentFilter);
        channelItemBroadcastReceiver.addObserver(this);
    }

    @Override
    public void updateOnPause() {
        unSubscribe();
    }

    private void unSubscribe() {
        localBroadcastManager.unregisterReceiver(channelItemBroadcastReceiver);
        channelItemBroadcastReceiver.removeObserver(this);
    }

    @Override
    public void updateOnReceiveItems(final ArrayList<?> items, final String action) {
        if (action.equals(ChannelItemBroadcastReceiver.getReceiveChannelItemsKey())
                || action.equals(ChannelItemBroadcastReceiver.getChannelItemsRefresh())) {
            final ArrayList<ChannelItem> receivedChannels = convertObjectToChannelItemList(items);
            if (null == recyclerView.getLayoutManager()) {
                final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
                recyclerView.setLayoutManager(layoutManager);
            }
            if (!receivedChannels.isEmpty()) {
                final ChannelItemRecyclerAdapter channelAdapter
                        = new ChannelItemRecyclerAdapter(receivedChannels);
                recyclerView.setAdapter(channelAdapter);

                if (null != recyclerState) {
                    recyclerView.getLayoutManager().onRestoreInstanceState(recyclerState);
                }
            } else {
                recyclerView.setAdapter(new ChannelRecyclerEmptyAdapter());
            }
        } else if (action.equals(ChannelItemBroadcastReceiver.getNoInternetAction())) {
            showNetworkDialog();
        }
        swipeRefreshLayout.setRefreshing(false);
        updateController.turnOffUpdate();
    }

    @Override
    public void updateOnRestoreInstanceState(final Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            recyclerState = savedInstanceState.getParcelable(RECYCLER_STATE);
            if (savedInstanceState.getBoolean(SWIPE_LAYOUT_STATE)) {
                swipeRefreshLayout.setRefreshing(true);
            }
        }
    }

    @Override
    public void updateOnSavedInstanceState(final Bundle outState) {
        if (null != recyclerView) {
            outState.putParcelable(RECYCLER_STATE, recyclerView.getLayoutManager()
                    .onSaveInstanceState());
        }
        if (swipeRefreshLayout.isShown()) {
            outState.putBoolean(SWIPE_LAYOUT_STATE, true);
        }
        if (swipeRefreshLayout.isShown()) {
            outState.putBoolean(SWIPE_LAYOUT_STATE, true);
        }
    }

    private ArrayList<ChannelItem> convertObjectToChannelItemList(
            @NonNull final ArrayList<?> list) {
        final ArrayList<ChannelItem> result = new ArrayList<>();
        for (int i = 0, size = list.size(); i < size; i++) {
            final Object object = list.get(i);
            if (object instanceof ChannelItem) {
                result.add((ChannelItem) object);
            }
        }
        return result;
    }

    private void writeToPreferences(@NonNull final String channelUrl) {
        final SharedPreferences sharedPreferences
                = activity.getSharedPreferences(CHANNEL_ITEM_ACTIVITY_PREFS_URL
                , Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CHANNEL_ITEM_ACTIVITY_PREFS_URL, channelUrl);
        editor.apply();
    }

    private String readUrlFromPreferences() {
        final SharedPreferences sharedPreferences
                = activity.getSharedPreferences(CHANNEL_ITEM_ACTIVITY_PREFS_URL
                , Context.MODE_PRIVATE);
        return sharedPreferences.getString(CHANNEL_ITEM_ACTIVITY_PREFS_URL, null);
    }

    private void showNetworkDialog() {
        final NoInternetDialog noInternetDialog = new NoInternetDialog();
        final android.app.FragmentTransaction fragmentTransaction
                = activity.getFragmentManager().beginTransaction();
        fragmentTransaction.add(noInternetDialog, ChannelAddDialogFragment.getDialogFragmentTag());
        fragmentTransaction.commitAllowingStateLoss();
        networkDialogShowController.turnOnNetworkDialog();
    }

    private void readUrlFromIntent() {
        final Intent intent = activity.getIntent();
        if (null != intent) {
            writeToPreferences(intent.getStringExtra(CHANNEL_ITEM_ACTIVITY_PREFS_URL));
            channelUrl = intent.getStringExtra(CHANNEL_ITEM_ACTIVITY_PREFS_URL);
        }
    }

    public static String getPrefsUrlKey() {
        return CHANNEL_ITEM_ACTIVITY_PREFS_URL;
    }

    @Override
    protected void saveLocalThemeIdToPrefs(final int themeId) {
        final SharedPreferences preferences
                = activity.getSharedPreferences(LOCAL_THEME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(LOCAL_THEME, themeId);
        editor.apply();
    }

    @Override
    protected int readLocalThemeIdFromPrefs() {
        final SharedPreferences preferences
                = activity.getSharedPreferences(LOCAL_THEME, Context.MODE_PRIVATE);
        return preferences.getInt(LOCAL_THEME, 0);
    }
}
