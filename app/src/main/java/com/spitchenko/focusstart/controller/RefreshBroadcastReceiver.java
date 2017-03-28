package com.spitchenko.focusstart.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import lombok.NonNull;

/**
 * Date: 23.03.17
 * Time: 22:25
 *
 * @author anatoliy
 */
public final class RefreshBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
            final Intent intentService = new Intent(context, RssChannelIntentService.class);
            intentService.setAction(RssChannelIntentService.getRefreshKey());
            context.startService(intentService);
    }
}
