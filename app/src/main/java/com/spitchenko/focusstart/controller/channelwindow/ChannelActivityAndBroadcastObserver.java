package com.spitchenko.focusstart.controller.channelwindow;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Toast;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.controller.ActivityAndBroadcastObserver;
import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.userinterface.base.ChannelRecyclerEmptyAdapter;
import com.spitchenko.focusstart.userinterface.base.NoInternetDialog;
import com.spitchenko.focusstart.userinterface.channelwindow.ChannelAddDialogFragment;
import com.spitchenko.focusstart.userinterface.channelwindow.ChannelRecyclerAdapter;
import com.spitchenko.focusstart.userinterface.channelwindow.ChannelRefreshDialog;

import java.util.ArrayList;

import lombok.NonNull;

/**
 * Date: 31.03.17
 * Time: 19:40
 *
 * @author anatoliy
 */
public class ChannelActivityAndBroadcastObserver implements ActivityAndBroadcastObserver {
    private final static String CHANNEL_BROADCAST_OBSERVER
            = "com.spitchenko.focusstart.controller.channel_window.ChannelActivityAndBroadcastObserver";
    private final static String RECYCLER_STATE = CHANNEL_BROADCAST_OBSERVER + ".recyclerState";

    private final ChannelBroadcastReceiver channelBroadcastReceiver = new ChannelBroadcastReceiver();
    private LocalBroadcastManager localBroadcastManager;
    private AppCompatActivity activity;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Parcelable recyclerState;

    public ChannelActivityAndBroadcastObserver(final AppCompatActivity context) {
        this.activity = context;
    }

    @Override
    public void updateOnCreate(@Nullable final Bundle savedInstanceState) {
        activity.setContentView(R.layout.activity_channel);

        final Toolbar toolbar = (Toolbar) activity.findViewById(R.id.activity_channel_toolbar);
        activity.setSupportActionBar(toolbar);
        if (null != activity.getSupportActionBar()) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        recyclerView = (RecyclerView) activity.findViewById(R.id.activity_channel_recycler_view);
        swipeRefreshLayout
                = (SwipeRefreshLayout) activity.findViewById(R.id.activity_channel_swipe_refresh_layout);
        final FloatingActionButton fab = (FloatingActionButton) activity.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showAddChannelDialog();
            }
        });

        final ItemTouchHelper.SimpleCallback simpleItemTouchCallback
                = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder
                    , final RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int swipeDir) {
                final ChannelRecyclerAdapter channelAdapter
                        = (ChannelRecyclerAdapter) recyclerView.getAdapter();
                final Channel channelFromRecycler
                        = channelAdapter.getChannels().get(viewHolder.getAdapterPosition());
                channelAdapter.removeItem(viewHolder.getAdapterPosition());

                RssChannelIntentService.start(channelFromRecycler
                        , RssChannelIntentService.getRemoveChannelKey(), activity);
            }
        };

        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RssChannelIntentService.start(null
                        , RssChannelIntentService.getRefreshAllChannelsKey(), activity);
            }
        });
    }

    @Override
    public void updateOnResume() {
        localBroadcastManager = LocalBroadcastManager.getInstance(activity);
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ChannelBroadcastReceiver.getReceiveChannelsKey());
        intentFilter.addAction(ChannelBroadcastReceiver.getRefreshDialogKey());
        intentFilter.addAction(ChannelBroadcastReceiver.getRemoveAction());
        intentFilter.addAction(ChannelBroadcastReceiver.getIoExceptionAction());
        intentFilter.addAction(ChannelBroadcastReceiver.getNoInternetAction());
        localBroadcastManager.registerReceiver(channelBroadcastReceiver, intentFilter);
        channelBroadcastReceiver.addObserver(this);

        RssChannelIntentService.start(null, RssChannelIntentService.getReadChannelsKey(), activity);
    }

    @Override
    public void updateOnPause() {
        localBroadcastManager.unregisterReceiver(channelBroadcastReceiver);
        channelBroadcastReceiver.removeObserver(this);
    }

    @Override
    public void updateOnReceiveItems(final ArrayList<?> items, final String action) {
        final ArrayList<Channel> receivedChannels = convertArrayListToChannelList(items);
        if (null == recyclerView.getLayoutManager()) {
            final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
            recyclerView.setLayoutManager(layoutManager);
        }
        if (!receivedChannels.isEmpty()) {
            final ChannelRecyclerAdapter channelAdapter = new ChannelRecyclerAdapter(receivedChannels);
            recyclerView.setAdapter(channelAdapter);

            checkUpdates(receivedChannels);

        } else {
            recyclerView.setAdapter(new ChannelRecyclerEmptyAdapter());
            if (null != recyclerState) {
                recyclerView.getLayoutManager().onRestoreInstanceState(recyclerState);
            }
        }
        if (null != action) {
            if (action.equals(ChannelBroadcastReceiver.getNoInternetAction())) {
                showNetworkDialog();
            } else if (action.equals(ChannelBroadcastReceiver.getIoExceptionAction())) {
                final String toastContent = activity.getResources().getString(R.string.io_exception);
                Toast.makeText(activity, toastContent, Toast.LENGTH_LONG).show();
            }
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void checkUpdates(final ArrayList<Channel> receivedChannels) {
        final SharedPreferences preferences
                = RssChannelIntentService.getReadMessagesPreferences(activity);
        for (final Channel channel:receivedChannels) {
            final String message = preferences.getString(channel.getLink(), null);
            if (null != message) {
                showRefreshDialog(channel.getLink(), message);
            }
        }
    }

    void updateOnReceiveNotification(@NonNull final String url, @NonNull final String message) {
        showRefreshDialog(url, message);
    }

    @Override
    public void updateOnRestoreInstanceState(@Nullable final Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            recyclerState = savedInstanceState.getParcelable(RECYCLER_STATE);
        }
    }

    @Override
    public void updateOnSavedInstanceState(final Bundle outState) {
        if (null != recyclerView) {
            outState.putParcelable(RECYCLER_STATE, recyclerView.getLayoutManager().onSaveInstanceState());
        }
    }

    private ArrayList<Channel> convertArrayListToChannelList(@NonNull final ArrayList<?> list) {
        final ArrayList<Channel> result = new ArrayList<>();
        for (final Object object:list) {
            if (object instanceof Channel) {
                result.add((Channel) object);
            }
        }
        return result;
    }

    private void showAddChannelDialog() {
        final ChannelAddDialogFragment dialogFragment = new ChannelAddDialogFragment();
        final android.app.FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
        fragmentTransaction.add(dialogFragment, ChannelAddDialogFragment.getDialogFragmentTag());
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void showNetworkDialog() {
        final NoInternetDialog noInternetDialog = new NoInternetDialog();
        final android.app.FragmentTransaction fragmentTransaction
                = activity.getFragmentManager().beginTransaction();
        fragmentTransaction.add(noInternetDialog, ChannelAddDialogFragment.getDialogFragmentTag());
        fragmentTransaction.commit();
    }

    private void showRefreshDialog(@NonNull final String url, @NonNull final String message) {
        final SharedPreferences preferences
                = RssChannelIntentService.getReadMessagesPreferences(activity);
        final SharedPreferences.Editor edit = preferences.edit();
        edit.putString(url, null);
        edit.apply();

        final ChannelRefreshDialog channelRefreshDialog = new ChannelRefreshDialog();
        final Bundle refreshBundle = new Bundle();
        refreshBundle.putString(ChannelRefreshDialog.CHANNEL_URL, url);
        refreshBundle.putString(ChannelRefreshDialog.MESSAGE, message);
        channelRefreshDialog.setArguments(refreshBundle);
        final android.app.FragmentTransaction fragmentTransaction
                = activity.getFragmentManager().beginTransaction();
        fragmentTransaction.add(channelRefreshDialog, ChannelAddDialogFragment.getDialogFragmentTag());
        fragmentTransaction.commitAllowingStateLoss();
    }
}
