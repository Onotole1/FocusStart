package com.spitchenko.focusstart.userinterface.channellitemwindow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.spitchenko.focusstart.controller.channelitemwindow.ChannelItemActivityAndBroadcastObserver;
import com.spitchenko.focusstart.userinterface.base.BaseActivity;

import lombok.NonNull;

public final class ChannelItemActivity extends BaseActivity {

    private ChannelItemActivityAndBroadcastObserver channelItemActivityAndBroadcastObserver
            = new ChannelItemActivityAndBroadcastObserver(this);

	@Override
    public final void onCreate(@Nullable final Bundle savedInstanceState) {
        this.addObserver(channelItemActivityAndBroadcastObserver);
        super.onCreate(savedInstanceState);
	}

    @Override
	protected final void onResume() {
        this.addObserver(channelItemActivityAndBroadcastObserver);
        super.onResume();
	}

	@Override
	protected final void onPause() {
		super.onPause();
        if (isFinishing()) {
            this.removeObserver(channelItemActivityAndBroadcastObserver);
        }
	}

    @Override
	protected void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);
        this.removeObserver(channelItemActivityAndBroadcastObserver);
	}

    public static void start(@NonNull final String key, @NonNull final String link
            , @NonNull final Context context) {
        final Intent intentBrowser = new Intent(context, ChannelItemActivity.class);
        intentBrowser.putExtra(key, link);
        context.startActivity(intentBrowser);
    }
}
