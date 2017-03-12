package com.spitchenko.focusstart.channel_window;

/**
 * Date: 07.03.17
 * Time: 23:33
 *
 * @author anatoliy
 */
final class AtomTagEnum {
	enum atomTag {
		FEED("feed"),
		ENTRY("entry"),
		LINK("link"),
		ICON("icon"),
		LINK_HREF("href"),
		PUBLISHED("published"),
		UPDATED("updated"),
		TITLE("title"),
		SUBTITLE("subtitle"),
		DATE_PATTERN("yyyy-MM-dd'T'HH:mm:ssZ");

		final String text;

		atomTag(final String text) {
			this.text = text;
		}


		@Override
		public final String toString() {
			return text;
		}
	}
}
