package com.spitchenko.focusstart.parser;

/**
 * Date: 26.02.17
 * Time: 16:45
 *
 * @author anatoliy
 */
class RssTag {
	enum rssTag {
		ITEM("item"),
		CHANNEL("channel"),
		LINK("link"),
		DESCRIPTION("description"),
		IMAGE("image"),
		URL_RSS("url"),
		LAST_BUILD_DATE("lastBuildDate"),
		TITLE("title");

		final String text;

		rssTag(String text) {
			this.text = text;
		}
	}
}
