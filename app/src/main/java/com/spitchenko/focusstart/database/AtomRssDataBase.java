package com.spitchenko.focusstart.database;

import android.provider.BaseColumns;

/**
 * Date: 09.03.17
 * Time: 18:14
 *
 * @author anatoliy
 */
public final class AtomRssDataBase {
	public static abstract class ChannelEntry implements BaseColumns {
		public static final String TABLE_NAME = "channels";
		static final String COLUMN_NAME_CHANNEL_ID = "channel_id";
		static final String COLUMN_NAME_CHANNEL_TITLE = "title";
		static final String COLUMN_NAME_CHANNEL_SUBTITLE = "subtitle";
		static final String COLUMN_NAME_CHANNEL_LAST_BUILD_DATE = "lastBuildDate";
		public static final String COLUMN_NAME_CHANNEL_LINK = "link";
		static final String COLUMN_NAME_CHANNEL_IMAGE = "image";
		public static final String COLUMN_NAME_CHANNEL_ISREAD = "isRead";
	}

	public static abstract class ChannelItemEntry implements BaseColumns {
		public static final String TABLE_NAME = "channel_items";
		static final String COLUMN_NAME_CHANNEL_ITEM_ID = "channel_item_id";
		static final String COLUMN_NAME_CHANNEL_ITEM_TITLE = "title";
		static final String COLUMN_NAME_CHANNEL_ITEM_SUBTITLE = "subtitle";
		static final String COLUMN_NAME_CHANNEL_ITEM_PUB_DATE = "pubDate";
		public static final String COLUMN_NAME_CHANNEL_ITEM_LINK = "link";
		static final String COLUMN_NAME_CHANNEL_ITEM_UPDATE_DATE = "updateDate";
		public static final String COLUMN_NAME_CHANNEL_ITEM_ISREAD = "isRead";
	}
}
