package com.spitchenko.focusstart.channel_iItem_window;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.spitchenko.focusstart.database.AtomRssChannelDbHelper;
import com.spitchenko.focusstart.database.AtomRssDataBase;
import com.spitchenko.focusstart.channel_window.Channel;
import com.spitchenko.focusstart.model.ChannelItem;

/**
 * Date: 13.03.17
 * Time: 0:23
 *
 * @author anatoliy
 */
public final class RssChannelItemReadIntentService extends IntentService {
	private final static String NAME = "RssChannelItemReadIntentService";

	public RssChannelItemReadIntentService() {
		super(NAME);
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		if (null != intent) {
			final ChannelItem inputChannel = intent.getParcelableExtra(ChannelItem.getKEY());
			if (!inputChannel.isRead()) {
				final AtomRssChannelDbHelper atomRssChannelDbHelper = new AtomRssChannelDbHelper(this);
				atomRssChannelDbHelper.updateValueFromDb(AtomRssDataBase.ChannelItemEntry.TABLE_NAME, AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_ISREAD
						, Long.toString(atomRssChannelDbHelper.boolToLong(true))
						, AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_LINK, inputChannel.getLink().toString());
				inputChannel.setRead(true);

				Intent broadcastIntent = new Intent("com.spitchenko.focusstart.LOAD_CHANNEL_ITEM");
				broadcastIntent.setPackage(getPackageName());
				broadcastIntent.putExtra(Channel.getKEY(), inputChannel);
				LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
			}
		}

	}
}
