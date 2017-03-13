package com.spitchenko.focusstart.channel_window;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.spitchenko.focusstart.database.AtomRssChannelDbHelper;

/**
 * Date: 09.03.17
 * Time: 15:18
 *
 * @author anatoliy
 */
public final class RssChannelIntentService extends IntentService {
	private final static String KEY = "URLS";
	private final static String NAME = "RssChannelIntentService";

	public RssChannelIntentService() {
		super(NAME);
	}

	@Override
	protected final void onHandleIntent(@Nullable final Intent intent) {
		final AtomRssParser atomRssParser = new AtomRssParser();
		final ArrayList<Channel> channels = new ArrayList<>();
		final ArrayList<URL> urls = new ArrayList<>();
		final AtomRssChannelDbHelper atomRssChannelDbHelper = new AtomRssChannelDbHelper(this);

		if (null != intent) {
			final Object input = intent.getExtras().get(KEY);
			if (input instanceof ArrayList<?>) {
				for (int i = 0; i < ((ArrayList) input).size(); i++) {
					if (((ArrayList) input).get(i) instanceof String) {
						try {
							urls.add(new URL((String) ((ArrayList) input).get(i)));
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		for (final URL singleUrl : urls) {
			final Channel channelFromDb = atomRssChannelDbHelper.readChannelFromDb(singleUrl);
			final Channel channelFromUrl = atomRssParser.parseXml(singleUrl);
			if (null == channelFromDb.getLink()) {
				channels.add(channelFromUrl);
				atomRssChannelDbHelper.writeChannelToDb(channelFromUrl);
			} else if (channelFromDb.getLastBuildDate().getTime() > channelFromUrl.getLastBuildDate().getTime() ||
			           channelFromDb.getLastBuildDate().getTime() == channelFromUrl.getLastBuildDate().getTime()) {
				channels.add(channelFromDb);
			} else if (channelFromDb.getLastBuildDate().getTime() < channelFromUrl.getLastBuildDate().getTime()) {
				channels.add(channelFromUrl);
				atomRssChannelDbHelper.deleteChannelFromDb(channelFromDb);
				atomRssChannelDbHelper.writeChannelToDb(channelFromUrl);
			}
		}

		for (final Channel channel:channels) {
			final Intent broadcastIntent = new Intent("com.spitchenko.focusstart.LOAD_CHANNEL");
			broadcastIntent.setPackage(getPackageName());
			broadcastIntent.putExtra(Channel.getKEY(), channel);
			LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
		}
	}

}
