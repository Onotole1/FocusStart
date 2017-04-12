package com.spitchenko.focusstart.userinterface.channelwindow;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.controller.channelwindow.RssChannelIntentService;

/**
 * Date: 24.03.17
 * Time: 15:20
 *
 * @author anatoliy
 */
public final class ChannelRefreshDialog extends DialogFragment {
    public final static String REFRESH_DIALOG =
            "com.spitchenko.focusstart.userinterface.channelwindow.ChannelRefreshDialog";
    public final static String MESSAGE = REFRESH_DIALOG + ".message";
    public final static String CHANNEL_URL = REFRESH_DIALOG + ".channelUrl";

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
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
}
