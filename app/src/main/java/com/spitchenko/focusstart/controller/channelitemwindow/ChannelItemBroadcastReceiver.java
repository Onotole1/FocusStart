package com.spitchenko.focusstart.controller.channelitemwindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.spitchenko.focusstart.model.ChannelItem;

import java.util.ArrayList;

import lombok.NonNull;

/**
 * Date: 11.03.17
 * Time: 17:06
 *
 * @author anatoliy
 */
public final class ChannelItemBroadcastReceiver extends BroadcastReceiver {
	private final static String CHANNEL_ITEM_BROADCAST_RECEIVER = "com.spitchenko.focusstart.ChannelItemBroadcastReceiver";
    private final static String RECEIVE_CHANNEL_ITEMS = CHANNEL_ITEM_BROADCAST_RECEIVER + ".receive";
    private final static String NO_INTERNET_ACTION = CHANNEL_ITEM_BROADCAST_RECEIVER + ".noInet";
    private final static String CHANNEL_REFRESH = CHANNEL_ITEM_BROADCAST_RECEIVER + ".refresh";

	private final ArrayList<ChannelItemActivityAndBroadcastObserver> observers = new ArrayList<>();
	private final ArrayList<ChannelItem> receivedChannels = new ArrayList<>();

    @Override
	public final void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        if (intent.getAction().equals(RECEIVE_CHANNEL_ITEMS)
                || intent.getAction().equals(CHANNEL_REFRESH)) {
            final ArrayList<ChannelItem> channelItems
                    = intent.getParcelableArrayListExtra(ChannelItem.getKEY());
            receivedChannels.clear();
            receivedChannels.addAll(channelItems);
            notifyObservers(RECEIVE_CHANNEL_ITEMS);
        } else if (intent.getAction().equals(NO_INTERNET_ACTION)) {
            notifyObservers(NO_INTERNET_ACTION);
        }
	}

	public void addObserver(@NonNull final ChannelItemActivityAndBroadcastObserver observer) {
		observers.add(observer);
	}

	public void removeObserver(@NonNull final ChannelItemActivityAndBroadcastObserver observer) {
		final int index = observers.indexOf(observer);
		if (index >= 0) {
			observers.remove(index);
		}
	}

	public void notifyObservers(@Nullable final String action) {
		for (final ChannelItemActivityAndBroadcastObserver observer:observers) {
			observer.updateOnReceiveItems(receivedChannels, action);
		}
	}

    public static String getNoInternetAction() {
        return NO_INTERNET_ACTION;
    }

    public static String getChannelItemsRefresh() {
        return CHANNEL_REFRESH;
    }

    public static String getReceiveChannelItemsKey() {
        return RECEIVE_CHANNEL_ITEMS;
    }

    public static void start(@NonNull final String action, @NonNull final String packageName
            , @NonNull final Context context, @Nullable final ArrayList<ChannelItem> channelItems) {
        final Intent broadcastIntent = new Intent(action);
        broadcastIntent.setPackage(packageName);
        if (null != channelItems) {
            broadcastIntent.putParcelableArrayListExtra(ChannelItem.getKEY(), channelItems);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(broadcastIntent);
    }
}
