package com.spitchenko.focusstart.asynctasks;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.spitchenko.focusstart.model.Channel;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Date: 24.02.17
 * Time: 21:52
 *
 * @author anatoliy
 */
public final class RssChannelAsyncTask extends AsyncTask<URL, Void, Void> {
	private enum rssTag {
		ITEM {
			@Override
			public String toString() {
				return "item";
			}
		},
		CHANNEL {
			@Override
			public String toString() {
				return "channel";
			}
		},
		LINK {
			@Override
			public String toString() {
				return "link";
			}
		},
		DESCRIPTION {
			@Override
			public String toString() {
				return "description";
			}
		},
		IMAGE {
			@Override
			public String toString() {
				return "image";
			}
		},
		URL_RSS {
			@Override
			public String toString() {
				return "url";
			}
		},
		LAST_BUILD_DATE {
			@Override
			public String toString() {
				return "lastBuildDate";
			}
		},
		TITLE {
			@Override
			public String toString() {
				return "title";
			}
		}
	}

	private XmlPullParser xpp;
	private Handler mHandler;
	private List<Channel> mChannels;

	public RssChannelAsyncTask(final Handler handler) {


		mHandler = handler;
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
	protected Void doInBackground(URL... params) {
		for (URL singleUrl : params) {
			mChannels.add(getChannel(singleUrl));
		}


		final Message msg = new Message();
		msg.obj = mChannels;
		mHandler.sendMessage(msg);
		return null;
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
			boolean insideImage = false;

			int eventType = xpp.getEventType();
			while (true) {
				if (eventType == XmlPullParser.START_TAG) {

					if (xpp.getName().equalsIgnoreCase(rssTag.CHANNEL.toString())) {
						insideChannel = true;
					} else if (xpp.getName().equalsIgnoreCase(rssTag.IMAGE.toString())) {
						insideImage = true;
					}
					else if (xpp.getName().equalsIgnoreCase(rssTag.TITLE.toString()) && !insideImage) {
						if (insideChannel) {
							title = xpp.nextText();
						}
					} else if (xpp.getName().equalsIgnoreCase(rssTag.LINK.toString()) && !insideImage) {
						if (insideChannel) {
							link = new URL(xpp.nextText());
						}
					} else if (xpp.getName().equalsIgnoreCase(rssTag.DESCRIPTION.toString())) {
						if (insideChannel) {
							subtitle = xpp.nextText();
						}
					} else if (xpp.getName().equalsIgnoreCase(rssTag.URL_RSS.toString()) && insideImage) {
						if (insideChannel) {
							image = new URL(xpp.nextText());
						}
					} else if (xpp.getName().equalsIgnoreCase(rssTag.LAST_BUILD_DATE.toString())) {
						if (insideChannel) {
							lastBuildDate = xpp.nextText();
						}
					} else if (xpp.getName().equalsIgnoreCase(rssTag.ITEM.toString())) {
						break;
					}
				} else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase(rssTag.CHANNEL.toString())) {
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
