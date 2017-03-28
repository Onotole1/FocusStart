package com.spitchenko.focusstart.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.spitchenko.focusstart.model.ChannelItem;
import com.spitchenko.focusstart.userinterface.channellitem.ChannelItemActivity;

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

	private final ArrayList<ChannelItemActivity> observers = new ArrayList<>();
	private final ArrayList<ChannelItem> receivedChannels = new ArrayList<>();

	@Override
	public final void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
		final ChannelItem channel = intent.getParcelableExtra(ChannelItem.getKEY());
		receivedChannels.add(channel);
		notifyObservers();
	}

	public void addObserver(@NonNull final ChannelItemActivity observer) {
		observers.add(observer);
	}

	public void removeObserver(@NonNull final ChannelItemActivity observer) {
		final int index = observers.indexOf(observer);
		if (index >= 0) {
			observers.remove(index);
		}
	}

	public void notifyObservers() {
		for (final ChannelItemActivity observer:observers) {
			observer.update(receivedChannels);
		}
	}

	public static String getChannelItemReceiverKey() {
        return CHANNEL_ITEM_BROADCAST_RECEIVER;
    }
}
