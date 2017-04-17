package com.spitchenko.focusstart.utils.parser;

import lombok.NonNull;

/**
 * Date: 26.02.17
 * Time: 16:45
 *
 * @author anatoliy
 */
final class RssTagEnum {
	enum RssTagEnumeration {
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
		DATE_PATTERN("dd MMM yyyy HH:mm:ss Z");

		final String text;

		RssTagEnumeration(@NonNull final String text) {
			this.text = text;
		}


		@Override
		public final String toString() {
			return text;
		}
	}
}
