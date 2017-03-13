package com.spitchenko.focusstart.channel_window;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.spitchenko.focusstart.database.AtomRssChannelDbHelper;
import com.spitchenko.focusstart.database.AtomRssDataBase;

/**
 * Date: 13.03.17
 * Time: 0:23
 *
 * @author anatoliy
 */
public final class RssChannelReadIntentService extends IntentService {
	private final static String NAME = "RssChannelItemReadIntentService";

	public RssChannelReadIntentService() {
		super(NAME);
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		if (null != intent) {
			final Channel inputChannel = intent.getParcelableExtra(Channel.getKEY());
			if (!inputChannel.isRead()) {
				final AtomRssChannelDbHelper atomRssChannelDbHelper = new AtomRssChannelDbHelper(this);
				atomRssChannelDbHelper.updateValueFromDb(AtomRssDataBase.ChannelEntry.TABLE_NAME, AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_ISREAD
						, Long.toString(atomRssChannelDbHelper.boolToLong(true))
						, AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_LINK, inputChannel.getLink().toString());
				inputChannel.setRead(true);

				Intent broadcastIntent = new Intent("com.spitchenko.focusstart.LOAD_CHANNEL");
				broadcastIntent.setPackage(getPackageName());
				broadcastIntent.putExtra(Channel.getKEY(), inputChannel);
				LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
			}
		}

	}
}
