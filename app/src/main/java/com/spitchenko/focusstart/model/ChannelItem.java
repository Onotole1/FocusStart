package com.spitchenko.focusstart.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import lombok.Getter;
import lombok.NonNull;
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
	private @Getter @Setter Date pubDate;
	private @Getter @Setter Date updateDate;
	private @Getter @Setter String link;
	private @Getter @Setter boolean isRead;

	public ChannelItem() {
	}

	private ChannelItem(@NonNull final Parcel in) {
		title = in.readString();
		subtitle = in.readString();
		pubDate = (Date) in.readSerializable();
		updateDate = (Date) in.readSerializable();
		link = in.readString();
		isRead = in.readByte() != 0;
	}

	public static final Creator<ChannelItem> CREATOR = new Creator<ChannelItem>() {
		@Override
		public ChannelItem createFromParcel(@NonNull final Parcel source) {
			return new ChannelItem(source);
		}

		@Override
		public ChannelItem[] newArray(final int size) {
			return new ChannelItem[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeString(title);
		dest.writeString(subtitle);
		dest.writeSerializable(pubDate);
		dest.writeSerializable(updateDate);
		dest.writeString(link);
		dest.writeByte((byte) (isRead ? 1 : 0));
	}
}
