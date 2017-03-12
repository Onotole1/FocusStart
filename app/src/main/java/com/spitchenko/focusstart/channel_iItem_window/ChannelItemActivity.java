package com.spitchenko.focusstart.channel_iItem_window;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.model.ChannelItem;

public final class ChannelItemActivity extends AppCompatActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel_item);

		Intent intent = getIntent();
		ArrayList<ChannelItem> channelItems = intent.getParcelableArrayListExtra(ChannelItem.getKEY());

		RecyclerView recyclerView = (RecyclerView) findViewById(R.id.activity_channel_item_recycler_view);
		RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);
		ChannelItemRecyclerAdapter channelItemRecyclerAdapter = new ChannelItemRecyclerAdapter(channelItems);
		recyclerView.setAdapter(channelItemRecyclerAdapter);
	}
}
