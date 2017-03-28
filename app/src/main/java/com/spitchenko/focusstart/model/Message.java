package com.spitchenko.focusstart.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Getter;

/**
 * Date: 27.03.17
 * Time: 17:37
 *
 * @author anatoliy
 */
public final class Message implements Parcelable {
    private @Getter final String url;
    private @Getter final String message;

    public Message(final String url, final String message) {
        this.url = url;
        this.message = message;
    }

    private Message(final Parcel in) {
        url = in.readString();
        message = in.readString();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(final Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(final int size) {
            return new Message[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(url);
        dest.writeString(message);
    }
}
