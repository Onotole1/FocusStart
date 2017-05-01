package com.spitchenko.focusstart.channelitemwindow.controller;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.base.controller.ChannelRecyclerEmptyAdapter;
import com.spitchenko.focusstart.base.controller.NetworkDialogShowController;
import com.spitchenko.focusstart.base.controller.NoInternetDialog;
import com.spitchenko.focusstart.base.view.BaseActivity;
import com.spitchenko.focusstart.channelitemwindow.view.ChannelItemFragment;
import com.spitchenko.focusstart.channelwindow.controller.ChannelAddDialogFragment;
import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.model.ChannelItem;
import com.spitchenko.focusstart.observer.FragmentAndBroadcastObserver;

import java.util.ArrayList;

import lombok.NonNull;

/**
 * Date: 01.04.17
 * Time: 14:01
 *
 * @author anatoliy
 */
public final class ChannelItemFragmentAndBroadcastObserver implements FragmentAndBroadcastObserver {
    private final static String CHANNEL_FRAGMENT_OBSERVER
            = "com.spitchenko.focusstart.controller.channel_item_window " +
            ".ChannelItemFragmentAndBroadcastObserver";
    private final static String CHANNEL_ITEM_FRAGMENT_PREFS_URL
            = CHANNEL_FRAGMENT_OBSERVER + ".channelPrefsUrl";
    private final static String CHANNEL_ITEM_FRAGMENT_PREFS_TITLE
            = CHANNEL_FRAGMENT_OBSERVER + ".channelPrefsTitle";
    private final static String RECYCLER_STATE = CHANNEL_FRAGMENT_OBSERVER + ".recyclerState";
    private final static String SWIPE_LAYOUT_STATE = CHANNEL_FRAGMENT_OBSERVER + ".swipeState";

    private android.app.Fragment fragment;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Channel channel;
    private Parcelable recyclerState;
    private LocalBroadcastManager localBroadcastManager;
    private ChannelItemBroadcastReceiver channelItemBroadcastReceiver;
    private NetworkDialogShowController networkDialogShowController;


    public ChannelItemFragmentAndBroadcastObserver(final android.app.Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void updateOnCreateView(final View view) {
        initToolbar();
        initSwipeRefreshLayout(view);
        initRecyclerView(view);
        initControllers();
    }

    @Override
    public void updateOnCreate(@Nullable final Bundle savedInstanceState) {

        if (null == savedInstanceState) {
            readChannelFromBundle();
        }

    }

    private void initToolbar() {
        final BaseActivity activity = (BaseActivity) fragment.getActivity();

        final android.support.v7.widget.Toolbar toolbar
                = (android.support.v7.widget.Toolbar) activity
                .findViewById(R.id.activity_main_toolbar);

        if (null != channel) {
            toolbar.setTitle(channel.getTitle());
        } else {
            toolbar.setTitle(readTitleFromPreferences(activity));
        }

        if (null != activity.getSupportActionBar()) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        activity.setSupportActionBar(toolbar);
    }

    private void initSwipeRefreshLayout(final View view) {
        swipeRefreshLayout
                = (SwipeRefreshLayout) view
                .findViewById(R.id.fragment_channel_item_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RssChannelItemIntentService.start(null, channel.getLink()
                        , RssChannelItemIntentService.getRefreshChannelItemsKey()
                        , fragment.getActivity());
            }
        });
    }

    private void initRecyclerView(final View view) {
        recyclerView = (RecyclerView) view
                .findViewById(R.id.fragment_channel_item_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(fragment.getActivity()));
        if (null != channel) {
            recyclerView.setAdapter(new ChannelItemRecyclerAdapter(channel.getChannelItems()));
        } else {
            recyclerView.setAdapter(new ChannelRecyclerEmptyAdapter());
            RssChannelItemIntentService.start(null
                    , readUrlFromPreferences((BaseActivity) fragment.getActivity())
                    , RssChannelItemIntentService.getReadChannelKey(), fragment.getActivity());
        }
    }

    private void initControllers() {
        final Activity activity = fragment.getActivity();

        networkDialogShowController = new NetworkDialogShowController(activity);
    }

    @Override
    public void updateOnResume() {
        subscribe();
    }

    private void subscribe() {
        localBroadcastManager = LocalBroadcastManager.getInstance(fragment.getActivity());
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
                final RecyclerView.LayoutManager layoutManager
                        = new LinearLayoutManager(fragment.getActivity());
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
    }

    @Override
    public void updateOnRestoreInstanceState(final Bundle savedInstanceState) {
        initToolbar();
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

    private void showNetworkDialog() {
        final NoInternetDialog noInternetDialog = new NoInternetDialog();
        final android.app.FragmentTransaction fragmentTransaction
                = fragment.getFragmentManager().beginTransaction();
        fragmentTransaction.add(noInternetDialog, ChannelAddDialogFragment.getDialogFragmentTag());
        fragmentTransaction.commitAllowingStateLoss();
        networkDialogShowController.turnOnNetworkDialog();
    }

    private void readChannelFromBundle() {
        final Channel channel
                = fragment.getArguments().getParcelable(ChannelItemFragment.getChannelUrlKey());
        if (null != channel) {
            this.channel = channel;
            writeUrlToPreferences(channel.getLink());
            writeTitleToPreferences(channel.getTitle());
        }
    }

    private void writeUrlToPreferences(@NonNull final String channelUrl) {
        final SharedPreferences sharedPreferences
                = fragment.getActivity().getSharedPreferences(CHANNEL_ITEM_FRAGMENT_PREFS_URL
                , Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CHANNEL_ITEM_FRAGMENT_PREFS_URL, channelUrl);
        editor.apply();
    }

    private String readUrlFromPreferences(final BaseActivity baseActivity) {
        final SharedPreferences sharedPreferences
                = baseActivity.getSharedPreferences(CHANNEL_ITEM_FRAGMENT_PREFS_URL
                , Context.MODE_PRIVATE);
        return sharedPreferences.getString(CHANNEL_ITEM_FRAGMENT_PREFS_URL, null);
    }

    private void writeTitleToPreferences(@NonNull final String title) {
        final SharedPreferences sharedPreferences
                = fragment.getActivity().getSharedPreferences(CHANNEL_ITEM_FRAGMENT_PREFS_TITLE
                , Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CHANNEL_ITEM_FRAGMENT_PREFS_TITLE, title);
        editor.apply();
    }

    private String readTitleFromPreferences(final BaseActivity baseActivity) {
        final SharedPreferences sharedPreferences
                = baseActivity.getSharedPreferences(CHANNEL_ITEM_FRAGMENT_PREFS_TITLE
                , Context.MODE_PRIVATE);
        return sharedPreferences.getString(CHANNEL_ITEM_FRAGMENT_PREFS_TITLE, null);
    }
}
