package com.spitchenko.focusstart.parser;

/**
 * Date: 07.03.17
 * Time: 23:33
 *
 * @author anatoliy
 */
class AtomTagEnum {
	enum atomTag {
		FEED("feed"),
		ENTRY("entry"),
		LINK("link"),
		ICON("icon"),
		LINK_HREF("href"),
		LINK_ALTERNATE("alternate"),
		SUMMARY("summary"),
		PUBLISHED("published"),
		UPDATED("updated"),
		TITLE("title"),
		SUBTITLE("subtitle");

		final String text;

		atomTag(String text) {
			this.text = text;
		}


		@Override
		public String toString() {
			return text;
		}
	}
}
