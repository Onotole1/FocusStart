package com.spitchenko.focusstart.parser;

/**
 * Date: 26.02.17
 * Time: 16:45
 *
 * @author anatoliy
 */
class RssTagEnum {
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
		TITLE("title");

		final String text;

		rssTag(String text) {
			this.text = text;
		}


		@Override
		public String toString() {
			return text;
		}
	}
}
