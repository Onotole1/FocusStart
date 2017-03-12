package com.spitchenko.focusstart.channel_window;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Date: 13.03.17
 * Time: 0:23
 *
 * @author anatoliy
 */
public final class RssChannelReadIntentService extends IntentService {
	private final static String NAME = "RssChannelReadIntentService";

	public RssChannelReadIntentService() {
		super(NAME);
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {

	}
}
