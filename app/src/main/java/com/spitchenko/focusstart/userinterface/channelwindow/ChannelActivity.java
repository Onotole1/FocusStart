package com.spitchenko.focusstart.userinterface.channelwindow;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Toast;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.controller.ChannelBroadcastReceiver;
import com.spitchenko.focusstart.controller.RssChannelIntentService;
import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.model.Message;
import com.spitchenko.focusstart.userinterface.base.BaseActivity;
import com.spitchenko.focusstart.userinterface.base.ChannelRecyclerEmptyAdapter;

import java.util.ArrayList;

import lombok.NonNull;

public final class ChannelActivity extends BaseActivity {
    public static boolean isActivityRun;
	public final static String CHANNEL_ACTIVITY_NAME = "com.spitchenko.focusstart.userinterface.channelwindow.ChannelActivity";
	private final static String CHANNEL_ID_SCROLL_KEY = CHANNEL_ACTIVITY_NAME + ".channelScrollKey";
    private final static String REFRESH_ACTION = CHANNEL_ACTIVITY_NAME + ".refreshAction";
    private final static String NOINET_ACTION = CHANNEL_ACTIVITY_NAME + ".noInet";
    private final static String IO_EXCEPTION_ACTION = CHANNEL_ACTIVITY_NAME + ".IOException";

	private RecyclerView recyclerView;
	private LocalBroadcastManager bManager;
	private ChannelBroadcastReceiver channelBroadcastReceiver;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel);

		final Toolbar toolbar = (Toolbar) findViewById(R.id.activity_channel_toolbar);
		setSupportActionBar(toolbar);
		if (null != getSupportActionBar()) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
		}

		recyclerView = (RecyclerView) findViewById(R.id.activity_channel_recycler_view);
		final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(new ChannelRecyclerEmptyAdapter());
		final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
		itemTouchHelper.attachToRecyclerView(recyclerView);

		final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				showAddChannelDialog();
			}
		});

        final Intent input = getIntent();
        if (null != input && null == savedInstanceState) {
            if (input.getAction().equals(REFRESH_ACTION)) {
                final ArrayList<Parcelable> inputMessages
                        = input.getParcelableArrayListExtra(REFRESH_ACTION);
                for (final Parcelable key:inputMessages) {
                    if (key instanceof Message) {
                        showRefreshDialog(((Message) key).getUrl(), ((Message) key).getMessage());
                    }
                }
            }
        }
    }

	@Override
	protected final void onResume() {
		super.onResume();
       // restoreRecyclerScroll(SCROLL_POSITION);
		bManager = LocalBroadcastManager.getInstance(this);
        channelBroadcastReceiver = new ChannelBroadcastReceiver();
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ChannelBroadcastReceiver.getReceiveChannelsKey());
        intentFilter.addAction(ChannelBroadcastReceiver.getRefreshDialogKey());
        bManager.registerReceiver(channelBroadcastReceiver, intentFilter);
        channelBroadcastReceiver.addObserver(this);
        final Intent intent = new Intent(getApplicationContext(), RssChannelIntentService.class);
        intent.setAction(RssChannelIntentService.getReadChannelsKey());
        startService(intent);
        isActivityRun = true;
	}

	@Override
	protected final void onPause() {
		super.onPause();
       // saveRecyclerScroll(SCROLL_POSITION);
		bManager.unregisterReceiver(channelBroadcastReceiver);
		channelBroadcastReceiver.removeObserver(this);
        isActivityRun = false;
	}

	private void showAddChannelDialog() {
		final ChannelAddDialogFragment dialogFragment = new ChannelAddDialogFragment();
		final android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.add(dialogFragment, ChannelAddDialogFragment.getDialogFragmentTag());
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

    private void showRefreshDialog(final String url, final String message) {
        final ChannelRefreshDialog channelRefreshDialog = new ChannelRefreshDialog();
        final Bundle refreshBundle = new Bundle();
        refreshBundle.putString(ChannelRefreshDialog.CHANNEL_URL, url);
        refreshBundle.putString(ChannelRefreshDialog.MESSAGE, message);
        channelRefreshDialog.setArguments(refreshBundle);
        final android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(channelRefreshDialog, ChannelAddDialogFragment.getDialogFragmentTag());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

	public void update(final Object object, final String action) {
		final ArrayList<Channel> receivedChannels = convertObjectToChannelList(object);
		if (null == recyclerView.getLayoutManager()) {
			final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
			recyclerView.setLayoutManager(layoutManager);
		}
		if (!receivedChannels.isEmpty()) {
			final ChannelRecyclerAdapter channelAdapter = new ChannelRecyclerAdapter(receivedChannels);
			recyclerView.setAdapter(channelAdapter);
			restoreRecyclerState();
		} else {
			recyclerView.setAdapter(new ChannelRecyclerEmptyAdapter());
		}
		if (null != action) {
            switch (action) {
                case NOINET_ACTION:
                    showNetworkDialog();
                    break;
                case IO_EXCEPTION_ACTION:
                    Toast.makeText(this, getResources().getString(R.string.io_exception)
                            , Toast.LENGTH_LONG).show();
                    break;
                default:
                    showRefreshDialog(receivedChannels.get(0).getLink(), action);
                    break;
            }
        }
	}

    public void updateNew(@NonNull final String url, @NonNull final String message) {
        showRefreshDialog(url, message);
    }

    private void showNetworkDialog() {
        final NoInternetDialog noInternetDialog = new NoInternetDialog();
        final android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(noInternetDialog, ChannelAddDialogFragment.getDialogFragmentTag());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		if (null != recyclerView.getLayoutManager()) {
			outState.putParcelable(CHANNEL_ID_SCROLL_KEY, recyclerView.getLayoutManager().onSaveInstanceState());
		}
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (null != savedInstanceState) {
			recyclerState = savedInstanceState.getParcelable(CHANNEL_ID_SCROLL_KEY);
		}
	}

	ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
		@Override
		public boolean onMove(final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder
				, final RecyclerView.ViewHolder target) {
			return false;
		}

		@Override
		public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int swipeDir) {
			final ChannelRecyclerAdapter channelAdapter = (ChannelRecyclerAdapter)recyclerView.getAdapter();
			final Channel channelFromRecycler = channelAdapter.getChannels().get(viewHolder.getAdapterPosition());
			channelAdapter.removeItem(viewHolder.getAdapterPosition());

			final Intent intent = new Intent(getApplicationContext(), RssChannelIntentService.class);
			intent.setAction(RssChannelIntentService.getRemoveChannelKey());
			intent.putExtra(RssChannelIntentService.getRemoveChannelKey(), channelFromRecycler);
			startService(intent);
		}
	};

	public static String getRefreshKey() {
        return REFRESH_ACTION;
    }
    public static String getIoExceptionActionKey() {
        return IO_EXCEPTION_ACTION;
    }
    public static String getNoinetActionKey() {
        return NOINET_ACTION;
    }
}
