package com.spitchenko.focusstart.controller;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.spitchenko.focusstart.database.AtomRssChannelDbHelper;
import com.spitchenko.focusstart.database.AtomRssDataBase;
import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.model.ChannelItem;
import com.spitchenko.focusstart.userinterface.channellitem.ChannelItemActivity;

import java.util.ArrayList;

import lombok.NonNull;

/**
 * Date: 14.03.17
 * Time: 1:57
 *
 * @author anatoliy
 */
public final class RssChannelItemIntentService extends IntentService {
	private final static String NAME_ITEM_SERVICE = "com.spitchenko.focusstart.controller.RssChannelItemIntentService";
	public final static String READ_CURRENT_CHANNEL = NAME_ITEM_SERVICE + ".readCurrentChannelDb";
	public final static String READ_CHANNELS = NAME_ITEM_SERVICE + ".channelsDb";

	public RssChannelItemIntentService() {
		super(NAME_ITEM_SERVICE);
	}

	@Override
	protected final void onHandleIntent(@Nullable final Intent intent) {
		if (null != intent) {
			if (intent.getAction().equals(READ_CHANNELS)) {
				readChannelItemsFromDb(intent);
			} else if (intent.getAction().equals(READ_CURRENT_CHANNEL)) {
				readCurrentChannelFromDb(intent);
			}
		}
	}

	private void readChannelItemsFromDb(@NonNull final Intent intent) {
		final AtomRssChannelDbHelper atomChannelDbHelper = new AtomRssChannelDbHelper(this);
		final String channelUrl = intent.getStringExtra(ChannelItem.getKEY());
		final Channel inputChannel = atomChannelDbHelper.readChannelFromDb(channelUrl);
		if (null != inputChannel) {
			sendBroadcast(inputChannel.getChannelItems());
		}
	}

	private void readCurrentChannelFromDb(@NonNull final Intent intent) {
		final AtomRssChannelDbHelper atomChannelDbHelper = new AtomRssChannelDbHelper(this);
		final ChannelItem inputChannelItem = intent.getParcelableExtra(ChannelItem.getKEY());
		final SharedPreferences sharedPreferences = getSharedPreferences(ChannelItemActivity
                .getChannelItemIdPreferencesKey(), Context.MODE_PRIVATE);
		final String channelUrl = sharedPreferences.getString(ChannelItemActivity
                .getChannelItemIdPreferencesKey(), null);

		if (!inputChannelItem.isRead()) {
			final Channel inputChannel = atomChannelDbHelper.readChannelFromDb(channelUrl);
			atomChannelDbHelper.updateValueFromDb(AtomRssDataBase.ChannelItemEntry.TABLE_NAME, AtomRssDataBase.ChannelItemEntry.CHANNEL_ITEM_ISREAD
					, Long.toString(atomChannelDbHelper.boolToLong(true))
					, AtomRssDataBase.ChannelItemEntry.CHANNEL_ITEM_LINK, inputChannelItem.getLink());
			inputChannelItem.setRead(true);

			if (null != inputChannel) {
				sendBroadcast(inputChannel.getChannelItems());
			}
		}
	}

	private void sendBroadcast(@NonNull final ArrayList<ChannelItem> channelItems) {
		for (final ChannelItem channelItem:channelItems) {
			final Intent broadcastIntent = new Intent(ChannelItemBroadcastReceiver.getChannelItemReceiverKey());
			broadcastIntent.setPackage(getPackageName());
			broadcastIntent.putExtra(ChannelItem.getKEY(), channelItem);
			LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
		}
	}
}
