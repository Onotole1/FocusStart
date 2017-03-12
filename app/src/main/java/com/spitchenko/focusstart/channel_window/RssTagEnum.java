package com.spitchenko.focusstart.channel_window;

/**
 * Date: 26.02.17
 * Time: 16:45
 *
 * @author anatoliy
 */
final class RssTagEnum {
	enum rssTag {
		RSS("rss"),
		ITEM("item"),
		CHANNEL("channel"),
		LINK("link"),
		DESCRIPTION("description"),
		IMAGE("image"),
		URL_RSS("url"),
		LAST_BUILD_DATE("lastBuildDate"),
		PUB_DATE("pubDate"),
		TITLE("title"),
		DATE_PATTERN("dd MMM yyyy HH:mm:ss Z"); // 12 Mar 2017 10:07:58 +0300");

		final String text;

		rssTag(final String text) {
			this.text = text;
		}


		@Override
		public final String toString() {
			return text;
		}
	}
}
