package com.spitchenko.focusstart.model;

import java.net.URL;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Getter;
import lombok.Setter;

/**
 * Date: 24.02.17
 * Time: 12:05
 *
 * @author anatoliy
 */
public final class ChannelItem implements Parcelable {
	private final static @Getter String KEY = "CHANNEL_ITEM";

	private @Getter @Setter String title;
	private @Getter @Setter String subtitle;
	private @Getter @Setter String pubDate;
	private @Getter @Setter String updateDate;
	private @Getter @Setter URL link;
	private @Getter @Setter boolean isRead;

	public ChannelItem() {
	}

	private ChannelItem(Parcel in) {
		title = in.readString();
		subtitle = in.readString();
		pubDate = in.readString();
		updateDate = in.readString();
		link = (URL)in.readSerializable();
		isRead = in.readByte() != 0;
	}

	public static final Creator<ChannelItem> CREATOR = new Creator<ChannelItem>() {
		@Override
		public ChannelItem createFromParcel(Parcel in) {
			return new ChannelItem(in);
		}

		@Override
		public ChannelItem[] newArray(int size) {
			return new ChannelItem[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(subtitle);
		dest.writeString(pubDate);
		dest.writeString(updateDate);
		dest.writeSerializable(link);
		dest.writeByte((byte) (isRead ? 1 : 0));
	}
}
