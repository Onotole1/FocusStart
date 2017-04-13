package com.spitchenko.focusstart.userinterface.channelwindow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.spitchenko.focusstart.controller.channelwindow.ChannelActivityAndBroadcastObserver;
import com.spitchenko.focusstart.observer.ActivityAndBroadcastObserver;
import com.spitchenko.focusstart.userinterface.base.BaseActivity;

import lombok.NonNull;

public final class ChannelActivity extends BaseActivity {

    private ActivityAndBroadcastObserver channelActivityAndBroadcastObserver
            = new ChannelActivityAndBroadcastObserver(this);

    @Override
    public final void onCreate(@Nullable final Bundle savedInstanceState) {
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
	protected void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);
        this.removeObserver(channelActivityAndBroadcastObserver);
	}

    public static void start(@NonNull final Context context) {
        final Intent activityIntent = new Intent(context, ChannelActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activityIntent.setAction(ChannelActivityAndBroadcastObserver.getUpdateKey());
        context.startActivity(activityIntent);
    }
}
