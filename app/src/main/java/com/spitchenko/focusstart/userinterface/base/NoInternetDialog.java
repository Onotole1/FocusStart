package com.spitchenko.focusstart.userinterface.base;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.controller.NetworkDialogShowController;

/**
 * Date: 26.03.17
 * Time: 20:16
 *
 * @author anatoliy
 */
public final class NoInternetDialog extends DialogFragment {
    private static final String NO_INTERNET_DIALOG_KEY
            = "com.spitchenko.focusstart.userinterface.base.NoInternetDialog";

    @Override
    public final Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final NetworkDialogShowController networkDialogController
                = new NetworkDialogShowController(getActivity());
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.no_internet_dialog_internet_message)
                .setPositiveButton(R.string.no_internet_dialog_turn_on_internet, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        networkDialogController.turnOffNetworkDialog();
                        final Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        networkDialogController.turnOffNetworkDialog();
                    }
                });
        return builder.create();
    }

    public static String getNoInternetDialogKey() {
        return NO_INTERNET_DIALOG_KEY;
    }
}
