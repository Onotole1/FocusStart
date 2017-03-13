package com.spitchenko.focusstart.channel_iItem_window;

import java.net.URL;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.model.ChannelItem;

public final class ChannelItemActivity extends AppCompatActivity {
	LocalBroadcastManager bManager;
	RecyclerView recyclerView;
	private URL channelUrl;
	private ChannelItemBroadcastReceiver channelItemBroadcastReceiver;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel_item);

		final Intent intent = getIntent();
		if (null != intent) {
			channelUrl = (URL) intent.getSerializableExtra(ChannelItem.getKEY());

		}
		recyclerView = (RecyclerView) findViewById(R.id.activity_channel_item_recycler_view);

	}

	@Override
	protected final void onResume() {
		super.onResume();
		bManager = LocalBroadcastManager.getInstance(this);
		final Intent intent = new Intent(getApplicationContext(), RssChannelItemIntentService.class);
		intent.putExtra(ChannelItem.getKEY(), channelUrl);
		startService(intent);
		channelItemBroadcastReceiver = new ChannelItemBroadcastReceiver(recyclerView);
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.spitchenko.focusstart.LOAD_CHANNEL_ITEM");
		bManager.registerReceiver(channelItemBroadcastReceiver, intentFilter);
	}

	@Override
	protected final void onPause() {
		super.onPause();
		bManager.unregisterReceiver(channelItemBroadcastReceiver);
	}
}
