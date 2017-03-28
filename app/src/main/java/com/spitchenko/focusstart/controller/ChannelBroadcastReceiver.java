package com.spitchenko.focusstart.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.userinterface.channelwindow.ChannelActivity;

import java.util.ArrayList;
import java.util.Iterator;

import lombok.NonNull;

/**
 * Date: 11.03.17
 * Time: 17:06
 *
 * @author anatoliy
 */
public final class ChannelBroadcastReceiver extends BroadcastReceiver {
	private final static String CHANNEL_BROADCAST_RECEIVER = "com.spitchenko.focusstart.ChannelBroadcastReceiver";
    private final static String RECEIVE_CHANNELS_KEY = CHANNEL_BROADCAST_RECEIVER + ".receive";
    private final static String REFRESH_DIALOG_KEY = CHANNEL_BROADCAST_RECEIVER + ".RefreshDialog";
    private final static String CHANNEL_URL_KEY = CHANNEL_BROADCAST_RECEIVER + ".ChannelUrl";
    private final static String MESSAGE_KEY = CHANNEL_BROADCAST_RECEIVER + ".MessageKey";

	private final ArrayList<ChannelActivity> observers = new ArrayList<>();
	private final ArrayList<Channel> receivedChannels = new ArrayList<>();

	@Override
	public final void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        if (intent.getAction().equals(RECEIVE_CHANNELS_KEY)) {
            final Channel channel = intent.getParcelableExtra(RECEIVE_CHANNELS_KEY);
            final String message = intent.getStringExtra(MESSAGE_KEY);

            if (null == message) {
                if (!containsChannel(receivedChannels, channel)) {
                    receivedChannels.add(channel);
                } else {
                    removeChannelFromList(receivedChannels, channel);
                    receivedChannels.add(channel);
                }
                notifyObservers(null);
            } else if (message.equals(RssChannelIntentService.getRemoveChannelKey())) {
                removeChannelFromList(receivedChannels, channel);
                notifyObservers(null);
            } else {
                receivedChannels.add(channel);
                notifyObservers(message);
            }
        } else if (intent.getAction().equals(REFRESH_DIALOG_KEY)) {
            final String url = intent.getStringExtra(CHANNEL_URL_KEY);
            final String message = intent.getStringExtra(MESSAGE_KEY);

            notifyObserversUpdate(url, message);
        }
	}

	public void addObserver(@NonNull final ChannelActivity observer) {
		observers.add(observer);
	}

	public void removeObserver(@NonNull final ChannelActivity observer) {
		final int index = observers.indexOf(observer);
		if (index >= 0) {
			observers.remove(index);
		}
	}

	public void notifyObservers(final String action) {
		for (final ChannelActivity observer:observers) {
			observer.update(receivedChannels, action);
		}
	}

    public void notifyObserversUpdate(@NonNull final String url, @NonNull final String message) {
        for (final ChannelActivity observer:observers) {
            observer.updateNew(url, message);
        }
    }

    private boolean containsChannel(final ArrayList<Channel> channels, final Channel channel) {
        for (final Channel received : channels) {
            if (received.getLink().equals(channel.getLink())) {
                channels.remove(received);
                channels.add(channel);
                notifyObservers(null);
                return true;
            }
        }
        return false;
    }

    private void removeChannelFromList(final ArrayList<Channel> channels, final Channel channel) {
        final Iterator<Channel> channelIterator = channels.iterator();
        while (channelIterator.hasNext()) {
            if (channelIterator.next().getLink().equals(channel.getLink())) {
                channelIterator.remove();
            }
        }

    }

    public static String getChannelUrlKey() {
        return CHANNEL_URL_KEY;
    }

    public static String getMessageKey() {
        return MESSAGE_KEY;
    }

    public static String getReceiveChannelsKey() {
        return RECEIVE_CHANNELS_KEY;
    }

    public static String getRefreshDialogKey() {
        return REFRESH_DIALOG_KEY;
    }
}
