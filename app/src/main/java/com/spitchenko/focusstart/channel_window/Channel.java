package com.spitchenko.focusstart.channel_window;

import java.net.URL;
import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.spitchenko.focusstart.model.ChannelItem;

import lombok.Getter;
import lombok.Setter;

/**
 * Date: 24.02.17
 * Time: 12:05
 *
 * @author anatoliy
 */
public final class Channel implements Parcelable {
	private final static @Getter String KEY = "CHANNEL";

	private @Getter @Setter	String title;
	private @Getter @Setter String subtitle;
	private @Getter @Setter Date lastBuildDate;
	private @Getter @Setter URL link;
	private @Getter @Setter URL image;
	private @Getter @Setter boolean isRead;
	private @Getter @Setter List<ChannelItem> channelItems;

	public Channel() {
	}

	private Channel(Parcel in) {
		title = in.readString();
		subtitle = in.readString();
		link = (URL) in.readSerializable();
		image = (URL) in.readSerializable();
		lastBuildDate = (Date) in.readSerializable();
		isRead = in.readByte() != 0;
		channelItems = in.createTypedArrayList(ChannelItem.CREATOR);
	}

	public static final Creator<Channel> CREATOR = new Creator<Channel>() {
		@Override
		public Channel createFromParcel(Parcel in) {
			return new Channel(in);
		}

		@Override
		public Channel[] newArray(int size) {
			return new Channel[size];
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
		dest.writeSerializable(link);
		dest.writeSerializable(image);
		dest.writeSerializable(lastBuildDate);
		dest.writeByte((byte) (isRead ? 1 : 0));
		dest.writeTypedList(channelItems);
	}
}
