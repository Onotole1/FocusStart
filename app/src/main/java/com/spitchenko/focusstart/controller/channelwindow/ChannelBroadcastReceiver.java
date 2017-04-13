package com.spitchenko.focusstart.controller.channelwindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.spitchenko.focusstart.userinterface.base.ActivityAndBroadcastObserver;
import com.spitchenko.focusstart.model.Channel;

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
    private final static String MESSAGE_KEY = CHANNEL_BROADCAST_RECEIVER + ".MessageKey";
    private final static String NO_INTERNET_ACTION = CHANNEL_BROADCAST_RECEIVER + ".noInet";
    private final static String IO_EXCEPTION_ACTION = CHANNEL_BROADCAST_RECEIVER + ".IOException";
    private static final String REMOVE_ACTION = CHANNEL_BROADCAST_RECEIVER + ".remove";
    private static final String LOADING_ACTION = CHANNEL_BROADCAST_RECEIVER + ".load";

    private final ArrayList<ChannelActivityAndBroadcastObserver> observers = new ArrayList<>();
	private final ArrayList<Channel> receivedChannels = new ArrayList<>();

	@Override
	public final void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        switch (intent.getAction()) {
            case RECEIVE_CHANNELS_KEY:
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
                } else {
                    receivedChannels.add(channel);
                    notifyObservers(message);
                }
                break;
            case REFRESH_DIALOG_KEY:
                notifyObserversUpdate();
                break;
            case REMOVE_ACTION:
                final Channel removeChannel = intent.getParcelableExtra(REMOVE_ACTION);
                removeChannelFromList(receivedChannels, removeChannel);
                notifyObservers(null);
                break;
            case NO_INTERNET_ACTION:
                notifyObservers(NO_INTERNET_ACTION);
                break;
            case IO_EXCEPTION_ACTION:
                notifyObservers(IO_EXCEPTION_ACTION);
                break;
            case LOADING_ACTION:
                notifyObservers(LOADING_ACTION);
                break;
        }
	}

	public void addObserver(@NonNull final ChannelActivityAndBroadcastObserver observer) {
		observers.add(observer);
	}

	public void removeObserver(@NonNull final ChannelActivityAndBroadcastObserver observer) {
		final int index = observers.indexOf(observer);
		if (index >= 0) {
			observers.remove(index);
		}
	}

	public void notifyObservers(@Nullable final String action) {
		for (final ActivityAndBroadcastObserver observer:observers) {
			observer.updateOnReceiveItems(receivedChannels, action);
		}
	}

    public void notifyObserversUpdate() {
        for (final ChannelActivityAndBroadcastObserver observer:observers) {
            observer.updateOnReceiveNotification();
        }
    }

    private boolean containsChannel(@NonNull final ArrayList<Channel> channels
            , @NonNull final Channel channel) {
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

    private void removeChannelFromList(@NonNull final ArrayList<Channel> channels
            , @NonNull final Channel channel) {
        final Iterator<Channel> channelIterator = channels.iterator();
        while (channelIterator.hasNext()) {
            if (channelIterator.next().getLink().equals(channel.getLink())) {
                channelIterator.remove();
            }
        }

    }

    public static String getReceiveChannelsKey() {
        return RECEIVE_CHANNELS_KEY;
    }

    public static String getRefreshDialogKey() {
        return REFRESH_DIALOG_KEY;
    }

    public static String getNoInternetAction() {
        return NO_INTERNET_ACTION;
    }

    public static String getIoExceptionAction() {
        return IO_EXCEPTION_ACTION;
    }

    public static String getRemoveAction() {
        return REMOVE_ACTION;
    }

    public static String getLoadingAction() {
        return LOADING_ACTION;
    }

    public static void start(@Nullable final Parcelable extra, @NonNull final String action
            , @NonNull final String packageName, @NonNull final Context context) {
        final Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(action);
        broadcastIntent.setPackage(packageName);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        if (null != extra) {
            broadcastIntent.putExtra(action, extra);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }
}
