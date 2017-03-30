package com.spitchenko.focusstart.userinterface.channellitem;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.controller.ChannelItemBroadcastReceiver;
import com.spitchenko.focusstart.controller.RssChannelItemIntentService;
import com.spitchenko.focusstart.model.ChannelItem;
import com.spitchenko.focusstart.userinterface.base.BaseActivity;
import com.spitchenko.focusstart.userinterface.base.ChannelRecyclerEmptyAdapter;

import java.util.ArrayList;

import lombok.NonNull;

public final class ChannelItemActivity extends BaseActivity {
	private final static String CHANNEL_ITEM_ACTIVITY_NAME = "com.spitchenko.focusstart.userinterface.channellitem.ChannelItemActivity";
	private final static String CHANNEL_ITEM_ID_PREFERENCES_KEY = CHANNEL_ITEM_ACTIVITY_NAME + ".channelItemIdKey";
	private final static String CHANNEL_ITEM_SCROLL_KEY = CHANNEL_ITEM_ACTIVITY_NAME + ".scrollKey";
    private final static String NO_INTERNET_KEY = CHANNEL_ITEM_ACTIVITY_NAME + ".noInternetKey";

	LocalBroadcastManager bManager;
	private ChannelItemBroadcastReceiver channelItemBroadcastReceiver;
	private String channelUrl;
    private SwipeRefreshLayout swipeRefreshLayout;

	@Override
	protected final void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel_item);
        initViews();
        readUrlFromIntent();
	}

    private void initViews() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.activity_channel_item_toolbar);
        setSupportActionBar(toolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        swipeRefreshLayout
                = (SwipeRefreshLayout) findViewById(R.id.activity_channel_item_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                final Intent intent = new Intent(getApplicationContext()
                        , RssChannelItemIntentService.class);
                intent.putExtra(ChannelItem.getKEY(), channelUrl);
                intent.setAction(RssChannelItemIntentService.getRefreshChannelItemsKey());
                startService(intent);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.activity_channel_item_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ChannelRecyclerEmptyAdapter());
    }

    private void readUrlFromIntent() {
        final Intent intent = getIntent();
        if (null != intent) {
            writeToPreferences(intent.getStringExtra(CHANNEL_ITEM_ID_PREFERENCES_KEY));
            channelUrl = intent.getStringExtra(ChannelItem.getKEY());
        } else {
            channelUrl = readIdFromPreferences();
        }
    }

    @Override
	protected final void onResume() {
		super.onResume();
		final Intent intentRead = new Intent(getApplicationContext(), RssChannelItemIntentService.class);
		intentRead.setAction(RssChannelItemIntentService.getReadChannelsKey());
		intentRead.putExtra(ChannelItem.getKEY(), channelUrl);
		startService(intentRead);
		bManager = LocalBroadcastManager.getInstance(this);
		channelItemBroadcastReceiver = new ChannelItemBroadcastReceiver();
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ChannelItemBroadcastReceiver.getChannelItemReceiverKey());
		bManager.registerReceiver(channelItemBroadcastReceiver, intentFilter);
		channelItemBroadcastReceiver.addObserver(this);
	}

	@Override
	protected final void onPause() {
		super.onPause();
		bManager.unregisterReceiver(channelItemBroadcastReceiver);
		channelItemBroadcastReceiver.removeObserver(this);
	}

	private void writeToPreferences(@NonNull final String channelUrl) {
		final SharedPreferences sharedPreferences = getSharedPreferences(CHANNEL_ITEM_ID_PREFERENCES_KEY, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(CHANNEL_ITEM_ID_PREFERENCES_KEY, channelUrl);
		editor.apply();
	}

	private String readIdFromPreferences() {
		final SharedPreferences sharedPreferences = getSharedPreferences(CHANNEL_ITEM_ID_PREFERENCES_KEY, Context.MODE_PRIVATE);
		return sharedPreferences.getString(CHANNEL_ITEM_ID_PREFERENCES_KEY, null);
	}

	public void update(@NonNull final Object object, final String action) {
		final ArrayList<ChannelItem> receivedChannels = convertObjectToChannelItemList(object);
		if (null == recyclerView.getLayoutManager()) {
			final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
			recyclerView.setLayoutManager(layoutManager);
		}
		if (!receivedChannels.isEmpty()) {
			final ChannelItemRecyclerAdapter channelAdapter = new ChannelItemRecyclerAdapter(receivedChannels);
			recyclerView.setAdapter(channelAdapter);
			restoreRecyclerState();
		} else {
			recyclerView.setAdapter(new ChannelRecyclerEmptyAdapter());
		}
        swipeRefreshLayout.setRefreshing(false);
        if (null != action && action.equals(NO_INTERNET_KEY)) {
            showNetworkDialog();
        }
	}

    @Override
	protected void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);
		if (null != recyclerView.getLayoutManager()) {
			outState.putParcelable(CHANNEL_ITEM_SCROLL_KEY, recyclerView.getLayoutManager().onSaveInstanceState());
		}
	}

	@Override
	protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
        recyclerState = savedInstanceState.getParcelable(CHANNEL_ITEM_SCROLL_KEY);
	}

    public static String getChannelItemIdPreferencesKey() {
        return CHANNEL_ITEM_ID_PREFERENCES_KEY;
    }

    public static String getNoInternetKey() {
        return NO_INTERNET_KEY;
    }
}
