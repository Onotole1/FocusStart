package com.spitchenko.focusstart.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;

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
	private @Getter @Setter String link;
	private @Getter @Setter Bitmap image;
	private @Getter @Setter boolean isRead;
	private @Getter @Setter
	ArrayList<ChannelItem> channelItems;

	public Channel() {
	}


    private Channel(final Parcel in) {
        title = in.readString();
        subtitle = in.readString();
        link = in.readString();
        image = in.readParcelable(Bitmap.class.getClassLoader());
        isRead = in.readByte() != 0;
        channelItems = in.createTypedArrayList(ChannelItem.CREATOR);
    }

    public static final Creator<Channel> CREATOR = new Creator<Channel>() {
        @Override
        public Channel createFromParcel(final Parcel in) {
            return new Channel(in);
        }

        @Override
        public Channel[] newArray(final int size) {
            return new Channel[size];
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
        dest.writeString(link);
        dest.writeParcelable(image, flags);
        dest.writeByte((byte) (isRead ? 1 : 0));
        dest.writeTypedList(channelItems);
    }
}
