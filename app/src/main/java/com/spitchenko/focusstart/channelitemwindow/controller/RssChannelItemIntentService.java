package com.spitchenko.focusstart.channelitemwindow.controller;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.spitchenko.focusstart.base.controller.UpdateController;
import com.spitchenko.focusstart.database.AtomRssChannelDbHelper;
import com.spitchenko.focusstart.database.AtomRssDataBase;
import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.model.ChannelItem;
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
	private final static String NAME_ITEM_SERVICE
            = "com.spitchenko.focusstart.controller.channel_item_window.RssChannelItemIntentService";
	private final static String READ_CURRENT_CHANNEL_ITEM = NAME_ITEM_SERVICE + ".readCurrentChannelDb";
	private final static String READ_CHANNEL = NAME_ITEM_SERVICE + ".channelsDb";
    private final static String REFRESH_CHANNEL_ITEMS = NAME_ITEM_SERVICE + ".refresh";

    private UpdateController updateController;

	public RssChannelItemIntentService() {
		super(NAME_ITEM_SERVICE);
	}

	@Override
	protected final void onHandleIntent(@Nullable final Intent intent) {
        if (null == updateController) {
            updateController = new UpdateController(this);
        }
		if (null != intent) {
            switch (intent.getAction()) {
                case READ_CHANNEL:
                    readChannelItemsFromDb(intent);
                    break;
                case READ_CURRENT_CHANNEL_ITEM:
                    readCurrentChannelFromDb(intent);
                    break;
                case REFRESH_CHANNEL_ITEMS:
                    refreshChannelItems(intent);
                    break;
            }
		}
	}

    private void refreshChannelItems(@NonNull final Intent intent) {
        final String channelUrl = intent.getStringExtra(REFRESH_CHANNEL_ITEMS);
        final AtomRssChannelDbHelper atomRssDbHelper = new AtomRssChannelDbHelper(this);
        final AtomRssParser atomRssParser = new AtomRssParser();

        try {
            final Channel channelFromUrl = atomRssParser.parseXml(channelUrl);
            final Channel channelFromDb = atomRssDbHelper.readChannelFromDb(channelUrl);

            if (null != channelFromDb) {
                final ArrayList<ChannelItem> itemsAll
                        = new ArrayList<>(channelFromDb.getChannelItems().size());

                final ArrayList<ChannelItem> channelFromDbChannelItems = channelFromDb
                        .getChannelItems();
                for (int i = 0, size = channelFromDbChannelItems.size(); i < size; i++) {
                    final ChannelItem item = channelFromDbChannelItems.get(i);
                    itemsAll.add(item.cloneChannelItem());
                }
                final ArrayList<ChannelItem> channelFromUrlChannelItems = channelFromUrl
                        .getChannelItems();
                for (int i = 0, size = channelFromUrlChannelItems.size(); i < size; i++) {
                    final ChannelItem item = channelFromUrlChannelItems.get(i);
                    itemsAll.add(item.cloneChannelItem());
                }

                if (channelFromDb.getChannelItems().size() > countMatches(itemsAll)) {
                    atomRssDbHelper.refreshCurrentChannel(channelFromDb, channelFromUrl);
                    final Channel readableChannel = atomRssDbHelper.readChannelFromDb(channelFromUrl.getLink());
                    if (null != readableChannel) {
                        readableChannel.setRead(true);
                        atomRssDbHelper.deleteChannelFromDb(readableChannel);
                        atomRssDbHelper.writeChannelToDb(readableChannel);
                    }
                    updateController.turnOnUpdate();
                }
                final Channel result = atomRssDbHelper.readChannelFromDb(channelFromDb.getLink());
                if (null != result) {
                    sendChannelItemsToBroadcast(result.getChannelItems(), ChannelItemBroadcastReceiver.getChannelItemsRefresh());
                }
            }
        } catch (IOException | XmlPullParserException e) {
            sendChannelItemsToBroadcast(null, ChannelItemBroadcastReceiver.getNoInternetAction());
        }
    }

    private void readChannelItemsFromDb(@NonNull final Intent intent) {
		final AtomRssChannelDbHelper atomChannelDbHelper = new AtomRssChannelDbHelper(this);
		final String channelUrl = intent.getStringExtra(READ_CHANNEL);
		final Channel inputChannel = atomChannelDbHelper.readChannelFromDb(channelUrl);
		if (null != inputChannel) {
			sendChannelItemsToBroadcast(inputChannel.getChannelItems()
                    , ChannelItemBroadcastReceiver.getReceiveChannelItemsKey());
		}
	}

	private void readCurrentChannelFromDb(@NonNull final Intent intent) {
		final AtomRssChannelDbHelper atomChannelDbHelper = new AtomRssChannelDbHelper(this);
		final ChannelItem inputChannelItem = intent.getParcelableExtra(ChannelItem.getKEY());

		if (!inputChannelItem.isRead()) {
			final Channel inputChannel = intent.getParcelableExtra(READ_CURRENT_CHANNEL_ITEM);
			atomChannelDbHelper.updateValueFromDb(AtomRssDataBase.ChannelItemEntry.TABLE_NAME
                    , AtomRssDataBase.ChannelItemEntry.CHANNEL_ITEM_ISREAD
					, Long.toString(atomChannelDbHelper.boolToLong(true))
					, AtomRssDataBase.ChannelItemEntry.CHANNEL_ITEM_LINK, inputChannelItem.getLink());
			inputChannelItem.setRead(true);

			if (null != inputChannel) {
				sendChannelItemsToBroadcast(inputChannel.getChannelItems()
                        , ChannelItemBroadcastReceiver.getReceiveChannelItemsKey());
			}
		}
	}

	private void sendChannelItemsToBroadcast(@Nullable final ArrayList<ChannelItem> channelItems
            , @NonNull final String action) {
        if (null != channelItems) {
            ChannelItemBroadcastReceiver.start(action, getPackageName(), this, channelItems);
        } else {
            ChannelItemBroadcastReceiver.start(action, getPackageName(), this, null);
        }
	}

	public static String getRefreshChannelItemsKey() {
        return REFRESH_CHANNEL_ITEMS;
    }

    public static String getReadCurrentChannelKey() {
        return READ_CURRENT_CHANNEL_ITEM;
    }

    public static String getReadChannelKey() {
        return READ_CHANNEL;
    }

    public static void start(@Nullable final ChannelItem channelItem, @Nullable final String url
            , @NonNull final String key, @NonNull final Context activity) {
        final Intent intent = new Intent(activity
                , RssChannelItemIntentService.class);
        if (null != url) {
            intent.putExtra(key, url);
        }
        if (null != channelItem) {
            intent.putExtra(ChannelItem.getKEY(), channelItem);
        }
        intent.setAction(key);
        activity.startService(intent);
    }
}
