package com.spitchenko.focusstart.channelwindow.controller;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.spitchenko.focusstart.R;

/**
 * Date: 24.03.17
 * Time: 15:20
 *
 * @author anatoliy
 */
public final class ChannelRefreshDialog extends DialogFragment {
    private final static String REFRESH_DIALOG =
            "com.spitchenko.focusstart.channelwindow.controller.ChannelRefreshDialog";
    private final static String MESSAGE = REFRESH_DIALOG + ".message";
    private final static String CHANNEL_URL = REFRESH_DIALOG + ".channelUrl";

    @Override
    public final Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final Bundle input = getArguments();
        final String channelUrl = input.getString(CHANNEL_URL);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(input.getString(MESSAGE))
                .setPositiveButton(R.string.channel_refresh_dialog_reload_button, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        RssChannelIntentService.start(RssChannelIntentService.getRefreshCurrentChannelKey()
                                , getActivity(), null, channelUrl);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {

                    }
                });
        return builder.create();
    }

    public static String getMessageKey() {
        return MESSAGE;
    }

    public static String getChannelUrlKey() {
        return CHANNEL_URL;
    }
}
