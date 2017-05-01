package com.spitchenko.focusstart.channelwindow.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.base.view.BaseFragment;
import com.spitchenko.focusstart.channelwindow.controller.ChannelFragmentAndBroadcastObserver;
import com.spitchenko.focusstart.observer.FragmentAndBroadcastObserver;

import lombok.NonNull;

public final class ChannelFragment extends BaseFragment {
    private final static String CHANNEL_FRAGMENT
            = "com.spitchenko.focusstart.channelwindow.view.ChannelFragment";
    private final static String CHANNEL_FRAGMENT_NOTIFICATION = CHANNEL_FRAGMENT + ".notification";

    private FragmentAndBroadcastObserver channelFragmentAndBroadcastObserver
            = new ChannelFragmentAndBroadcastObserver(this);

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container
            , final Bundle savedInstanceState) {
        this.addObserver(channelFragmentAndBroadcastObserver);
        final View view = inflater.inflate(R.layout.fragment_channel, container, false);
        notifyObserversOnCreateView(view);
        return view;
    }

    @Override
    public final void onCreate(@Nullable final Bundle savedInstanceState) {
        this.addObserver(channelFragmentAndBroadcastObserver);
        super.onCreate(savedInstanceState);
    }

	@Override
	public final void onResume() {
        this.addObserver(channelFragmentAndBroadcastObserver);
		super.onResume();
	}

	@Override
	public final void onPause() {
		super.onPause();
        if (getActivity().isFinishing()) {
            this.removeObserver(channelFragmentAndBroadcastObserver);
        }
	}

    @Override
	public void onSaveInstanceState(@NonNull final Bundle outState) {
		super.onSaveInstanceState(outState);
        this.removeObserver(channelFragmentAndBroadcastObserver);
	}

    public static String getChannelFragmentKey() {
        return CHANNEL_FRAGMENT;
    }

    public static String getChannelFragmentNotificationKey() {
        return CHANNEL_FRAGMENT_NOTIFICATION;
    }
}
