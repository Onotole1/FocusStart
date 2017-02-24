package com.spitchenko.focusstart.asynctasks;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.spitchenko.focusstart.model.Channel;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


import static com.spitchenko.focusstart.asynctasks.RssChannelAsyncTask.rssTag.CHANNEL;
import static com.spitchenko.focusstart.asynctasks.RssChannelAsyncTask.rssTag.DESCRIPTION;
import static com.spitchenko.focusstart.asynctasks.RssChannelAsyncTask.rssTag.IMAGE;
import static com.spitchenko.focusstart.asynctasks.RssChannelAsyncTask.rssTag.ITEM;
import static com.spitchenko.focusstart.asynctasks.RssChannelAsyncTask.rssTag.LAST_BUILD_DATE;
import static com.spitchenko.focusstart.asynctasks.RssChannelAsyncTask.rssTag.LINK;
import static com.spitchenko.focusstart.asynctasks.RssChannelAsyncTask.rssTag.TITLE;
import static com.spitchenko.focusstart.asynctasks.RssChannelAsyncTask.rssTag.URL_RSS;

/**
 * Date: 24.02.17
 * Time: 21:52
 *
 * @author anatoliy
 */
public final class RssChannelAsyncTask extends AsyncTask<URL, Void, List<Channel>> {
	enum rssTag {
		ITEM("item"),
		CHANNEL("channel"),
		TITLE("title"),
		LINK("link"),
		DESCRIPTION("description"),
		IMAGE("image"),
		URL_RSS("url"),
		LAST_BUILD_DATE("lastBuildDate");

		private final String text;

		rssTag(final String text) {
			this.text = text;
		}
	}

	private XmlPullParser xpp;
	private List<Channel> mChannels;

	public RssChannelAsyncTask() {
		mChannels = new ArrayList<>();
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(false);
			xpp = factory.newPullParser();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected List<Channel> doInBackground(URL... params) {
		for (URL singleUrl : params) {
			mChannels.add(getChannel(singleUrl));
		}
		return mChannels;
	}

	private InputStream getInputStream(URL url) {
		try {
			return url.openConnection().getInputStream();
		} catch (IOException e) {
			return null;
		}
	}

	private Channel getChannel(URL singleUrl) {
		String title = null;
		String subtitle = null;
		String lastBuildDate = null;
		URL link = null;
		URL image = null;

		try {
			xpp.setInput(getInputStream(singleUrl), "UTF_8");

			boolean insideChannel = false;

			int eventType = xpp.getEventType();
			while (!xpp.getName().equalsIgnoreCase(ITEM.text)) {
				if (eventType == XmlPullParser.START_TAG) {

					if (xpp.getName().equalsIgnoreCase(CHANNEL.text)) {
						insideChannel = true;
					} else if (xpp.getName().equalsIgnoreCase(TITLE.text)) {
						if (insideChannel) {
							title = xpp.nextText();
						}
					} else if (xpp.getName().equalsIgnoreCase(LINK.text)) {
						if (insideChannel) {
							link = new URL(xpp.nextText());
						}
					} else if (xpp.getName().equalsIgnoreCase(DESCRIPTION.text)) {
						if (insideChannel) {
							subtitle = xpp.nextText();
						}
					} else if (xpp.getName().equalsIgnoreCase(IMAGE.text)) {
						while (!xpp.getName().equalsIgnoreCase(URL_RSS.text) || eventType != XmlPullParser.END_TAG) {
							eventType = xpp.next();
						}
						if (insideChannel) {
							image = new URL(xpp.nextText());
						}
					} else if (xpp.getName().equalsIgnoreCase(LAST_BUILD_DATE.text)) {
						if (insideChannel) {
							lastBuildDate = xpp.nextText();
						}
					}
				} else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase(CHANNEL.text)) {
					insideChannel = false;
				}

				eventType = xpp.next();
			}
		}catch(XmlPullParserException | IOException e){
			e.printStackTrace();
		}

		Channel singleChannel = new Channel();
		singleChannel.setTitle(title);
		singleChannel.setSubtitle(subtitle);
		singleChannel.setLastBuildDate(lastBuildDate);
		singleChannel.setLink(link);
		singleChannel.setImage(image);

		return singleChannel;
	}
}
