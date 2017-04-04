package com.spitchenko.focusstart.userinterface.channelwindow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.spitchenko.focusstart.controller.channelwindow.ChannelActivityAndBroadcastObserver;
import com.spitchenko.focusstart.userinterface.base.BaseActivity;

public final class ChannelActivity extends BaseActivity {

    private ChannelActivityAndBroadcastObserver channelActivityAndBroadcastObserver
            = new ChannelActivityAndBroadcastObserver(this);

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        this.addObserver(channelActivityAndBroadcastObserver);
        super.onCreate(savedInstanceState);
    }

	@Override
	protected final void onResume() {
        this.addObserver(channelActivityAndBroadcastObserver);
		super.onResume();
	}

	@Override
	protected final void onPause() {
		super.onPause();
        if (this.isFinishing()) {
            this.removeObserver(channelActivityAndBroadcastObserver);
        }
	}

    @Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
        this.removeObserver(channelActivityAndBroadcastObserver);
	}

    public static void start(final Context context) {
        final Intent activityIntent = new Intent(context, ChannelActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activityIntent.setAction(ChannelActivityAndBroadcastObserver.getUpdateKey());
        context.startActivity(activityIntent);
    }
}
