package com.spitchenko.focusstart.utils.parser;

/**
 * Date: 07.03.17
 * Time: 23:33
 *
 * @author anatoliy
 */
final class AtomTagEnum {
	enum AtomTags {
		FEED("feed"),
		ENTRY("entry"),
		LINK("link"),
		ICON("icon"),
		LINK_HREF("href"),
		PUBLISHED("published"),
		UPDATED("updated"),
		TITLE("title"),
		SUBTITLE("subtitle"),
        CONTENT("content"),
		DATE_PATTERN("yyyy-MM-dd'T'HH:mm:ssZ");

		final String text;

		AtomTags(final String text) {
			this.text = text;
		}


		@Override
		public final String toString() {
			return text;
		}
	}
}
