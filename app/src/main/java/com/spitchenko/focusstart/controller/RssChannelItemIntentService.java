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
import com.spitchenko.focusstart.utils.parser.AtomRssParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import lombok.NonNull;

import static com.spitchenko.focusstart.model.ChannelItem.countMatches;

/**
 * Date: 14.03.17
 * Time: 1:57
 *
 * @author anatoliy
 */
public final class RssChannelItemIntentService extends IntentService {
	private final static String NAME_ITEM_SERVICE = "com.spitchenko.focusstart.controller.RssChannelItemIntentService";
	private final static String READ_CURRENT_CHANNEL = NAME_ITEM_SERVICE + ".readCurrentChannelDb";
	private final static String READ_CHANNELS = NAME_ITEM_SERVICE + ".channelsDb";
    private final static String REFRESH_CHANNEL_ITEMS = NAME_ITEM_SERVICE + ".refresh";

	public RssChannelItemIntentService() {
		super(NAME_ITEM_SERVICE);
	}

	@Override
	protected final void onHandleIntent(@Nullable final Intent intent) {
		if (null != intent) {
            switch (intent.getAction()) {
                case READ_CHANNELS:
                    readChannelItemsFromDb(intent);
                    break;
                case READ_CURRENT_CHANNEL:
                    readCurrentChannelFromDb(intent);
                    break;
                case REFRESH_CHANNEL_ITEMS:
                    refreshChannelItems(intent);
                    break;
            }
		}
	}

    private void refreshChannelItems(@NonNull final Intent intent) {
        final String channelUrl = intent.getStringExtra(ChannelItem.getKEY());
        final AtomRssChannelDbHelper atomRssDbHelper = new AtomRssChannelDbHelper(this);
        final AtomRssParser atomRssParser = new AtomRssParser();

        try {
            final Channel channelFromUrl = atomRssParser.parseXml(channelUrl);
            final Channel channelFromDb = atomRssDbHelper.readChannelFromDb(channelUrl);

            if (null != channelFromDb) {
                final ArrayList<ChannelItem> itemsAll
                        = new ArrayList<>(channelFromDb.getChannelItems().size());

                for (final ChannelItem item : channelFromDb.getChannelItems()) {
                    itemsAll.add(item.cloneChannelItem());
                }
                for (final ChannelItem item : channelFromUrl.getChannelItems()) {
                    itemsAll.add(item.cloneChannelItem());
                }

                if (channelFromDb.getChannelItems().size() > countMatches(itemsAll)) {
                    atomRssDbHelper.refreshCurrentChannel(channelFromDb, channelFromUrl);
                }
                final Channel result = atomRssDbHelper.readChannelFromDb(channelFromDb.getLink());
                if (null != result) {
                    sendChannelItemsToBroadcast(result.getChannelItems(), null);
                }
            }
        } catch (IOException | XmlPullParserException e) {
            sendChannelItemsToBroadcast(null, ChannelItemActivity.getNoInternetKey());
        }
    }

    private void readChannelItemsFromDb(@NonNull final Intent intent) {
		final AtomRssChannelDbHelper atomChannelDbHelper = new AtomRssChannelDbHelper(this);
		final String channelUrl = intent.getStringExtra(ChannelItem.getKEY());
		final Channel inputChannel = atomChannelDbHelper.readChannelFromDb(channelUrl);
		if (null != inputChannel) {
			sendChannelItemsToBroadcast(inputChannel.getChannelItems(), null);
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
				sendChannelItemsToBroadcast(inputChannel.getChannelItems(), null);
			}
		}
	}

	private void sendChannelItemsToBroadcast(@Nullable final ArrayList<ChannelItem> channelItems, final String action) {
        if (null != channelItems) {
            for (final ChannelItem channelItem : channelItems) {
                final Intent broadcastIntent = new Intent(ChannelItemBroadcastReceiver.getChannelItemReceiverKey());
                broadcastIntent.setPackage(getPackageName());
                broadcastIntent.putExtra(ChannelItem.getKEY(), channelItem);
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
            }
        } else {
            final Intent broadcastIntent = new Intent(ChannelItemBroadcastReceiver.getChannelItemReceiverKey());
            broadcastIntent.putExtra(ChannelItemActivity.getNoInternetKey(), action);
            broadcastIntent.setPackage(getPackageName());
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        }
	}

	public static String getRefreshChannelItemsKey() {
        return REFRESH_CHANNEL_ITEMS;
    }

    public static String getReadCurrentChannelKey() {
        return READ_CURRENT_CHANNEL;
    }

    public static String getReadChannelsKey() {
        return READ_CHANNELS;
    }
}
