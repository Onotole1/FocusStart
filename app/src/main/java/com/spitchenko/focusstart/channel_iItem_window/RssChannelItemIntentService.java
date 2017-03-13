package com.spitchenko.focusstart.channel_iItem_window;

import java.net.URL;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.spitchenko.focusstart.channel_window.Channel;
import com.spitchenko.focusstart.database.AtomRssChannelDbHelper;
import com.spitchenko.focusstart.model.ChannelItem;

/**
 * Date: 14.03.17
 * Time: 1:57
 *
 * @author anatoliy
 */
public final class RssChannelItemIntentService extends IntentService {
	private final static String NAME = "RssChannelItemIntentService";

	public RssChannelItemIntentService() {
		super(NAME);
	}

	@Override
	protected final void onHandleIntent(@Nullable final Intent intent) {
		if (null != intent) {
			AtomRssChannelDbHelper atomRssChannelDbHelper = new AtomRssChannelDbHelper(this);
			final URL channelUrl = (URL) intent.getSerializableExtra(ChannelItem.getKEY());
			final Channel inputChannel = atomRssChannelDbHelper.readChannelFromDb(channelUrl);
			for (final ChannelItem channelItem:inputChannel.getChannelItems()) {
				final Intent broadcastIntent = new Intent("com.spitchenko.focusstart.LOAD_CHANNEL_ITEM");
				broadcastIntent.setPackage(getPackageName());
				broadcastIntent.putExtra(ChannelItem.getKEY(), channelItem);
				LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
			}
		}
	}
}
