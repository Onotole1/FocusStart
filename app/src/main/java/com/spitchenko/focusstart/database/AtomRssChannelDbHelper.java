package com.spitchenko.focusstart.database;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.spitchenko.focusstart.channel_window.Channel;
import com.spitchenko.focusstart.model.ChannelItem;

/**
 * Date: 09.03.17
 * Time: 18:36
 *
 * @author anatoliy
 */
public final class AtomRssChannelDbHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "AtomRssChannel.db";

	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_CHANNEL_ENTRIES =
			"CREATE TABLE " + AtomRssDataBase.ChannelEntry.TABLE_NAME + " (" +
			AtomRssDataBase.ChannelEntry._ID + " INTEGER PRIMARY KEY," +
			AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_ID + TEXT_TYPE + COMMA_SEP +
			AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_TITLE + TEXT_TYPE + COMMA_SEP +
			AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_SUBTITLE + TEXT_TYPE + COMMA_SEP +
			AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_LINK + TEXT_TYPE + COMMA_SEP +
			AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_IMAGE + TEXT_TYPE + COMMA_SEP +
			AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_LAST_BUILD_DATE + TEXT_TYPE + COMMA_SEP +
			AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_ISREAD + " INTEGER" +
		    " )";

	private static final String SQL_CREATE_CHANNEL_ITEM_ENTRIES =
			"CREATE TABLE " + AtomRssDataBase.ChannelItemEntry.TABLE_NAME + " (" +
			AtomRssDataBase.ChannelItemEntry._ID + " INTEGER PRIMARY KEY," +
			AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_ID + TEXT_TYPE + COMMA_SEP +
			AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_TITLE + TEXT_TYPE + COMMA_SEP +
			AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_SUBTITLE + TEXT_TYPE + COMMA_SEP +
			AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_LINK + TEXT_TYPE + COMMA_SEP +
			AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_PUB_DATE + TEXT_TYPE + COMMA_SEP +
			AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_UPDATE_DATE + TEXT_TYPE + COMMA_SEP +
			AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_ID + " INTEGER" + COMMA_SEP +
			AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_ISREAD + " INTEGER" + COMMA_SEP +
			"FOREIGN KEY(" + AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_ID +
			") REFERENCES " + AtomRssDataBase.ChannelEntry.TABLE_NAME + "(" + AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_ID + ")" +
			" )";

	private static final String SQL_DELETE_CHANNEL_ENTRIES =
			"DROP TABLE IF EXISTS " + AtomRssDataBase.ChannelEntry.TABLE_NAME;

	public AtomRssChannelDbHelper(final Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public final void onCreate(final SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_CHANNEL_ENTRIES);
		db.execSQL(SQL_CREATE_CHANNEL_ITEM_ENTRIES);
	}

	@Override
	public final void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		db.execSQL(SQL_DELETE_CHANNEL_ENTRIES);
		onCreate(db);
	}


	public final Channel readChannelFromDb(final URL url) {
		SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
		Channel channel = new Channel();

		Cursor cursorChannel = sqLiteDatabase.rawQuery("SELECT *  FROM  "
		                                               + AtomRssDataBase.ChannelEntry.TABLE_NAME + " WHERE "
		                                               + AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_LINK + "= '"
		                                               + url.toString() + "' ORDER BY "
		                                               + AtomRssDataBase.ChannelEntry._ID + " DESC", null);
		cursorChannel.moveToFirst();
		if (cursorChannel.getCount() > 0) {

			channel.setTitle(cursorChannel.getString(cursorChannel.getColumnIndex(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_TITLE)));
			channel.setSubtitle(cursorChannel.getString(cursorChannel.getColumnIndex(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_SUBTITLE)));
			channel.setRead(longToBool(cursorChannel.getLong(cursorChannel.getColumnIndex(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_ISREAD))));
			channel.setLastBuildDate(new Date(cursorChannel.getString(cursorChannel.getColumnIndex(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_LAST_BUILD_DATE))));
			try {
				channel.setLink(new URL(cursorChannel.getString(cursorChannel.getColumnIndex(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_LINK))));
				channel.setImage(new URL(cursorChannel.getString(cursorChannel.getColumnIndex(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_IMAGE))));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}


			ArrayList<ChannelItem> channelItems = readChannelItemsFromDb(sqLiteDatabase, cursorChannel.getLong(cursorChannel.getColumnIndex(AtomRssDataBase.ChannelEntry._ID)));
			channel.setChannelItems(channelItems);
		}
		cursorChannel.close();
		sqLiteDatabase.close();
		return channel;
	}

	public final Channel readChannelFromDb(final long id) {
		SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
		Channel channel = new Channel();

		Cursor cursorChannel = sqLiteDatabase.rawQuery("SELECT *  FROM  "
		                                               + AtomRssDataBase.ChannelEntry.TABLE_NAME + " WHERE "
		                                               + AtomRssDataBase.ChannelEntry._ID + "= '"
		                                               + id + "' ORDER BY "
		                                               + AtomRssDataBase.ChannelEntry._ID + " DESC", null);
		cursorChannel.moveToFirst();
		if (cursorChannel.getCount() > 0) {

			channel.setTitle(cursorChannel.getString(cursorChannel.getColumnIndex(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_TITLE)));
			channel.setSubtitle(cursorChannel.getString(cursorChannel.getColumnIndex(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_SUBTITLE)));
			channel.setRead(longToBool(cursorChannel.getLong(cursorChannel.getColumnIndex(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_ISREAD))));
			channel.setLastBuildDate(new Date(cursorChannel.getString(cursorChannel.getColumnIndex(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_LAST_BUILD_DATE))));
			try {
				channel.setLink(new URL(cursorChannel.getString(cursorChannel.getColumnIndex(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_LINK))));
				channel.setImage(new URL(cursorChannel.getString(cursorChannel.getColumnIndex(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_IMAGE))));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}


			ArrayList<ChannelItem> channelItems = readChannelItemsFromDb(sqLiteDatabase, cursorChannel.getLong(cursorChannel.getColumnIndex(AtomRssDataBase.ChannelEntry._ID)));
			channel.setChannelItems(channelItems);
		}
		cursorChannel.close();
		sqLiteDatabase.close();
		return channel;
	}

	private ArrayList<ChannelItem> readChannelItemsFromDb(final SQLiteDatabase sqLiteDatabase, final long channelId) {
		ArrayList<ChannelItem> channelItems = new ArrayList<>();
		Cursor cursorChannelItem = sqLiteDatabase.rawQuery("SELECT *  FROM  "
		                                                   + AtomRssDataBase.ChannelItemEntry.TABLE_NAME + " WHERE "
		                                                   + AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_ID + "= '"
		                                                   + channelId + "'", null);
		cursorChannelItem.moveToFirst();
		do {
			ChannelItem channelItem = new ChannelItem();
			channelItem.setTitle(cursorChannelItem.getString(cursorChannelItem.getColumnIndex(AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_TITLE)));
			channelItem.setSubtitle(cursorChannelItem.getString(cursorChannelItem.getColumnIndex(AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_SUBTITLE)));
			try {
				channelItem.setLink(new URL(cursorChannelItem.getString(cursorChannelItem.getColumnIndex(AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_LINK))));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			channelItem.setPubDate(cursorChannelItem.getString(cursorChannelItem.getColumnIndex(AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_PUB_DATE)));
			channelItem.setUpdateDate(cursorChannelItem.getString(cursorChannelItem.getColumnIndex(AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_UPDATE_DATE)));
			channelItem.setRead(longToBool(cursorChannelItem.getLong(cursorChannelItem.getColumnIndex(AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_ISREAD))));
			channelItems.add(channelItem);
		} while (cursorChannelItem.moveToNext());
		cursorChannelItem.close();
		sqLiteDatabase.close();

		return channelItems;
	}

	public final void writeChannelToDb(final Channel channel) {
		SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
		ContentValues values = new ContentValues();


		values.put(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_TITLE, channel.getTitle());
		values.put(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_SUBTITLE, channel.getSubtitle());
		values.put(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_LINK, channel.getLink().toString());
		if (null != channel.getImage()) {
			values.put(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_IMAGE, channel.getImage().toString());
		}
		values.put(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_LAST_BUILD_DATE, channel.getLastBuildDate().toString());
		values.put(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_ISREAD, boolToLong(channel.isRead()));
		sqLiteDatabase.insert(AtomRssDataBase.ChannelEntry.TABLE_NAME, null, values);
		values.clear();

		Cursor cursor = sqLiteDatabase.rawQuery("SELECT " + AtomRssDataBase.ChannelEntry._ID + "  FROM  "
		                                        + AtomRssDataBase.ChannelEntry.TABLE_NAME + " WHERE "
		                                        + AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_LAST_BUILD_DATE + "= '"
		                                        + channel.getLastBuildDate() + "' ORDER BY "
		                                        + AtomRssDataBase.ChannelEntry._ID + " DESC" , null);
		cursor.moveToFirst();
		long id = cursor.getLong(cursor.getColumnIndex(AtomRssDataBase.ChannelEntry._ID));

		cursor.close();

		for (ChannelItem channelItem:channel.getChannelItems()) {
			values.put(AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_TITLE, channelItem.getTitle());
			values.put(AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_SUBTITLE, channelItem.getSubtitle());
			values.put(AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_LINK, channelItem.getLink().toString());
			values.put(AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_PUB_DATE, channelItem.getPubDate());
			values.put(AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_UPDATE_DATE, channelItem.getUpdateDate());
			values.put(AtomRssDataBase.ChannelItemEntry.COLUMN_NAME_CHANNEL_ITEM_ISREAD, boolToLong(channelItem.isRead()));
			values.put(AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_ID, id);
			sqLiteDatabase.insert(AtomRssDataBase.ChannelItemEntry.TABLE_NAME, null, values);
			values.clear();
		}
		sqLiteDatabase.close();
	}

	public final void deleteChannelFromDb(final Channel channel) {
		SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
		Cursor cursor = sqLiteDatabase.rawQuery("SELECT " + AtomRssDataBase.ChannelEntry._ID + "  FROM  "
		                                        + AtomRssDataBase.ChannelEntry.TABLE_NAME + " WHERE "
		                                        + AtomRssDataBase.ChannelEntry.COLUMN_NAME_CHANNEL_LINK + "= '"
		                                        + channel.getLink().toString() + "'", null);
		cursor.moveToFirst();
		long id = cursor.getLong(cursor.getColumnIndex(AtomRssDataBase.ChannelEntry._ID));
		do {
			sqLiteDatabase.delete(AtomRssDataBase.ChannelItemEntry.TABLE_NAME, AtomRssDataBase.ChannelEntry._ID + "=" + id, null);
		} while (cursor.moveToNext());
		cursor.close();
		sqLiteDatabase.close();
	}

	public final void updateValueFromDb(final String tableName, final String columnValue, final String value, final String whereColumn, final String whereValue) {
		SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
		sqLiteDatabase.execSQL("UPDATE " + tableName + " SET " + columnValue + "=" + value + " WHERE " + whereColumn + "='" + whereValue + "'");
		sqLiteDatabase.close();
	}

	private boolean longToBool(final long number) {
		return number == 1;
	}

	public long boolToLong(final boolean bool) {
		return bool ? 1 : 0;
	}
}
