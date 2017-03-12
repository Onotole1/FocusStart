package com.spitchenko.focusstart.channel_window;

import java.util.ArrayList;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.spitchenko.focusstart.R;

public final class ChannelActivity extends AppCompatActivity {
	private final static String KEY = "URLS";
	private RecyclerView recyclerView;
	private LocalBroadcastManager bManager;
	private ChannelBroadcastReceiver channelBroadcastReceiver;

	@Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);
		recyclerView = new RecyclerView(this);

	    ArrayList<String> urls = new ArrayList<>();
	    urls.add("https://news.yandex.ru/index.rss");
	    urls.add("http://www.xn--atemschutzunflle-7nb.de/asu.xml");
	    Intent intent = new Intent(getApplicationContext(), RssChannelIntentService.class);
	    intent.putStringArrayListExtra(KEY, urls);
	    startService(intent);

		recyclerView = (RecyclerView) findViewById(R.id.activity_channel_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

    }

	@Override
	protected final void onResume() {
		super.onResume();
		bManager = LocalBroadcastManager.getInstance(this);
		channelBroadcastReceiver = new ChannelBroadcastReceiver(recyclerView);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.spitchenko.focusstart.LOAD_CHANNEL");
		bManager.registerReceiver(channelBroadcastReceiver, intentFilter);
	}

	@Override
	protected final void onPause() {
		super.onPause();
		bManager.unregisterReceiver(channelBroadcastReceiver);
	}
}
