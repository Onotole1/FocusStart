package com.spitchenko.focusstart.base.controller;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spitchenko.focusstart.R;

import lombok.NonNull;

/**
 * Date: 23.03.17
 * Time: 12:57
 *
 * @author anatoliy
 */
public class ChannelRecyclerEmptyAdapter extends RecyclerView.Adapter<RecyclerEmptyHolder>{

	public ChannelRecyclerEmptyAdapter() {
	}

	@Override
	public RecyclerEmptyHolder onCreateViewHolder(@NonNull final ViewGroup parent
            , final int viewType) {
		final View channelElement = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.channel_element_empty, parent, false);
		return new RecyclerEmptyHolder(channelElement);
	}

	@Override
	public void onBindViewHolder(@NonNull final RecyclerEmptyHolder holder
            , final int position) {
	}

	@Override
	public int getItemCount() {
		return 0;
	}
}
