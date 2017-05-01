package com.spitchenko.focusstart.channelitemwindow.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.base.view.BaseActivity;
import com.spitchenko.focusstart.base.view.BaseFragment;
import com.spitchenko.focusstart.channelitemwindow.controller.ChannelItemFragmentAndBroadcastObserver;
import com.spitchenko.focusstart.model.Channel;

import lombok.NonNull;

public final class ChannelItemFragment extends BaseFragment {
    private final static String CHANNEL_ITEM_FRAGMENT
            = "com.spitchenko.focusstart.channelitemwindow.view.ChannelItemFragment";
    private final static String CHANNEL_URL = CHANNEL_ITEM_FRAGMENT + ".channelUrl";

    private ChannelItemFragmentAndBroadcastObserver channelItemActivityAndBroadcastObserver
            = new ChannelItemFragmentAndBroadcastObserver(this);

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container
            , @Nullable final Bundle savedInstanceState) {
        this.addObserver(channelItemActivityAndBroadcastObserver);
        final View view = inflater.inflate(R.layout.fragment_channel_item, container, false);
        notifyObserversOnCreateView(view);
        return view;
    }

    @Override
    public final void onCreate(@Nullable final Bundle savedInstanceState) {
        this.addObserver(channelItemActivityAndBroadcastObserver);
        super.onCreate(savedInstanceState);
	}

    @Override
	public final void onResume() {
        this.addObserver(channelItemActivityAndBroadcastObserver);
        super.onResume();
	}

	@Override
	public final void onPause() {
		super.onPause();
        if (getActivity().isFinishing()) {
            this.removeObserver(channelItemActivityAndBroadcastObserver);
        }
	}

    @Override
	public void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);
        this.removeObserver(channelItemActivityAndBroadcastObserver);
	}

    public static void start(@NonNull final Channel channel, @NonNull final BaseActivity activity) {
        activity.setChannelItemFragment(channel);
    }

    public static String getChannelItemFragmentKey() {
        return CHANNEL_ITEM_FRAGMENT;
    }

    public static String getChannelUrlKey() {
        return CHANNEL_URL;
    }
}
