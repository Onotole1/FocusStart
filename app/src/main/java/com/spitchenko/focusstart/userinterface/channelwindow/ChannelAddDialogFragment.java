package com.spitchenko.focusstart.userinterface.channelwindow;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.controller.channelwindow.RssChannelIntentService;

import lombok.NonNull;

/**
 * Date: 19.03.17
 * Time: 16:12
 *
 * @author anatoliy
 */
public final class ChannelAddDialogFragment extends DialogFragment {
    private final static String DIALOG_FRAGMENT_TAG = "dialogTag";
    private final static String CHANNELS_PREFERENCES_KEY = "channels_preferences";

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @SuppressLint("InflateParams")
    @Override
    public final Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View promptsView = inflater.inflate(R.layout.add_channel_dialog, null);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.add_channel_dialog_input_edittext);
        final SharedPreferences sharedPreferences
                = getActivity().getSharedPreferences(CHANNELS_PREFERENCES_KEY, Context.MODE_PRIVATE);

        readFromPreferences(sharedPreferences, userInput);
        builder.setView(promptsView)
                .setPositiveButton(R.string.channel_add_dialog_add_button, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        writeToPreferences(userInput.getText().toString(), sharedPreferences);

                        RssChannelIntentService.start(RssChannelIntentService
                                .getReadWriteActionKey(), getActivity(), null
                                , userInput.getText().toString());
                    }
                })
                .setNegativeButton(R.string.dialog_cancel_button, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        writeToPreferences(userInput.getText().toString(), sharedPreferences);
                    }
                });
        return builder.create();
    }

    private void writeToPreferences(@NonNull final String input
            , @NonNull final SharedPreferences sharedPreferences) {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CHANNELS_PREFERENCES_KEY, input);
        editor.apply();
    }

    private void readFromPreferences(@NonNull final SharedPreferences sharedPreferences
            , @NonNull final EditText editText) {
        final String sharedString = sharedPreferences.getString(CHANNELS_PREFERENCES_KEY, "");
        editText.setText(sharedString);
        editText.setSelection(sharedString.length());
    }

    public static String getDialogFragmentTag() {
        return DIALOG_FRAGMENT_TAG;
    }
}