package com.spitchenko.focusstart.controller.channelitemwindow;

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
import com.spitchenko.focusstart.controller.ActivityAndBroadcastObserver;
import com.spitchenko.focusstart.model.ChannelItem;
import com.spitchenko.focusstart.userinterface.base.ChannelRecyclerEmptyAdapter;
import com.spitchenko.focusstart.userinterface.base.NoInternetDialog;
import com.spitchenko.focusstart.userinterface.channellitem.ChannelItemRecyclerAdapter;
import com.spitchenko.focusstart.userinterface.channelwindow.ChannelAddDialogFragment;

import java.util.ArrayList;

import lombok.NonNull;

/**
 * Date: 01.04.17
 * Time: 14:01
 *
 * @author anatoliy
 */
public final class ChannelItemActivityAndBroadcastObserver implements ActivityAndBroadcastObserver {
    private final static String CHANNEL_ITEM_ACTIVITY_OBSERVER
            = "com.spitchenko.focusstart.controller.channel_item_window.ChannelItemActivityAndBroadcastObserver";
    private final static String CHANNEL_ITEM_ACTIVITY_PREFS_URL
            = CHANNEL_ITEM_ACTIVITY_OBSERVER + ".sharedUrl";
    private final static String RECYCLER_STATE = CHANNEL_ITEM_ACTIVITY_OBSERVER + ".state";

    private AppCompatActivity activity;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String channelUrl;
    private Parcelable recyclerState;
    private LocalBroadcastManager localBroadcastManager;
    private ChannelItemBroadcastReceiver channelItemBroadcastReceiver;
    private int recyclerScroll = 0;


    public ChannelItemActivityAndBroadcastObserver(final AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override
    public void updateOnCreate(@Nullable final Bundle savedInstanceState) {
        activity.setContentView(R.layout.activity_channel_item);

        final Toolbar toolbar = (Toolbar) activity.findViewById(R.id.activity_channel_item_toolbar);
        activity.setSupportActionBar(toolbar);
        if (null != activity.getSupportActionBar()) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        swipeRefreshLayout
                = (SwipeRefreshLayout) activity.findViewById(R.id.activity_channel_item_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RssChannelItemIntentService.start(null, channelUrl
                        , RssChannelItemIntentService.getRefreshChannelItemsKey(), activity);
            }
        });

        recyclerView = (RecyclerView) activity.findViewById(R.id.activity_channel_item_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(new ChannelRecyclerEmptyAdapter());

        if (null == savedInstanceState) {
            readUrlFromIntent();
        }

        channelUrl = readIdFromPreferences();
    }

    @Override
    public void updateOnResume() {
        recyclerScroll = getRecyclerScrollFromPreferences();
        RssChannelItemIntentService.start(null, channelUrl
                , RssChannelItemIntentService.getReadChannelsKey(), activity);
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
                final ChannelItemRecyclerAdapter channelAdapter = new ChannelItemRecyclerAdapter(receivedChannels);
                recyclerView.setAdapter(channelAdapter);

                if (null != recyclerState) {
                    recyclerView.getLayoutManager().onRestoreInstanceState(recyclerState);
                } else {
                    final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    layoutManager.scrollToPosition(recyclerScroll);
                }
            } else {
                recyclerView.setAdapter(new ChannelRecyclerEmptyAdapter());
            }
        } else if (action.equals(ChannelItemBroadcastReceiver.getNoInternetAction())) {
            showNetworkDialog();
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void updateOnRestoreInstanceState(final Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            recyclerState = savedInstanceState.getParcelable(RECYCLER_STATE);
        }
    }

    @Override
    public void updateOnSavedInstanceState(final Bundle outState) {
        writeRecyclerScrollToPrefs();
        if (null != recyclerView) {
            outState.putParcelable(RECYCLER_STATE, recyclerView.getLayoutManager().onSaveInstanceState());
        }
    }

    private ArrayList<ChannelItem> convertObjectToChannelItemList(@NonNull final ArrayList<?> list) {
        final ArrayList<ChannelItem> result = new ArrayList<>();
        for (final Object object:list) {
            if (object instanceof ChannelItem) {
                result.add((ChannelItem) object);
            }
        }
        return result;
    }

    private void writeToPreferences(@NonNull final String channelUrl) {
        final SharedPreferences sharedPreferences
                = activity.getSharedPreferences(CHANNEL_ITEM_ACTIVITY_PREFS_URL, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CHANNEL_ITEM_ACTIVITY_PREFS_URL, channelUrl);
        editor.apply();
    }

    private String readIdFromPreferences() {
        final SharedPreferences sharedPreferences
                = activity.getSharedPreferences(CHANNEL_ITEM_ACTIVITY_PREFS_URL, Context.MODE_PRIVATE);
        return sharedPreferences.getString(CHANNEL_ITEM_ACTIVITY_PREFS_URL, null);
    }

    private void showNetworkDialog() {
        final NoInternetDialog noInternetDialog = new NoInternetDialog();
        final android.app.FragmentTransaction fragmentTransaction
                = activity.getFragmentManager().beginTransaction();
        fragmentTransaction.add(noInternetDialog, ChannelAddDialogFragment.getDialogFragmentTag());
        fragmentTransaction.commit();
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

    private int getRecyclerScrollFromPreferences() {
        final SharedPreferences sharedPreferences = activity.getSharedPreferences(RECYCLER_STATE
                , Context.MODE_PRIVATE);
        return sharedPreferences.getInt(RECYCLER_STATE, 0);
    }

    private void writeRecyclerScrollToPrefs() {
        if (recyclerView != null) {
            final SharedPreferences sharedPreferences = activity.getSharedPreferences(RECYCLER_STATE,
                    Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            final LinearLayoutManager linearLayoutManager
                    = (LinearLayoutManager) recyclerView.getLayoutManager();
            editor.putInt(RECYCLER_STATE, linearLayoutManager.findFirstVisibleItemPosition());
            editor.apply();
        }
    }
}
