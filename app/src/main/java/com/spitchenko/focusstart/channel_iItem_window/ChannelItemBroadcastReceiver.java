package com.spitchenko.focusstart.channel_iItem_window;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.spitchenko.focusstart.model.ChannelItem;

/**
 * Date: 11.03.17
 * Time: 17:06
 *
 * @author anatoliy
 */
public final class ChannelItemBroadcastReceiver extends BroadcastReceiver {
	private final ArrayList<ChannelItem> receivedChannels = new ArrayList<>();
	private final RecyclerView recyclerView;

	public ChannelItemBroadcastReceiver(final RecyclerView recyclerView) {
		this.recyclerView = recyclerView;
	}

	@Override
	public final void onReceive(final Context context, final Intent intent) {
		final ChannelItem channel = intent.getParcelableExtra(ChannelItem.getKEY());
		receivedChannels.add(channel);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
		recyclerView.setLayoutManager(layoutManager);
		ChannelItemRecyclerAdapter channelRecyclerAdapter = new ChannelItemRecyclerAdapter(receivedChannels);
		recyclerView.setAdapter(channelRecyclerAdapter);
	}
}
