package com.spitchenko.focusstart.channel_window;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;

/**
 * Date: 11.03.17
 * Time: 17:06
 *
 * @author anatoliy
 */
public final class ChannelBroadcastReceiver extends BroadcastReceiver {
	private final ArrayList<Channel> receivedChannels = new ArrayList<>();
	private final RecyclerView recyclerView;

	public ChannelBroadcastReceiver(RecyclerView recyclerView) {
		this.recyclerView = recyclerView;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Channel channel = intent.getParcelableExtra(Channel.getKEY());
		receivedChannels.add(channel);
		ChannelRecyclerAdapter channelRecyclerAdapter = new ChannelRecyclerAdapter(receivedChannels);
		recyclerView.setAdapter(channelRecyclerAdapter);
	}
}
