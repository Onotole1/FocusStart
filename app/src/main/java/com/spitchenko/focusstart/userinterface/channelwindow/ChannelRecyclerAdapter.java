package com.spitchenko.focusstart.userinterface.channelwindow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.controller.RssChannelIntentService;
import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.model.ChannelItem;
import com.spitchenko.focusstart.userinterface.channellitem.ChannelItemActivity;

import java.util.ArrayList;

import lombok.Getter;

/**
 * Date: 24.02.17
 * Time: 0:18
 *
 * @author anatoliy
 */
final class ChannelRecyclerAdapter extends RecyclerView.Adapter<ChannelRecyclerViewHolder> {
	private final @Getter ArrayList<Channel> channels;
	private Context context;

	ChannelRecyclerAdapter(final ArrayList<Channel> channels) {
		this.channels = channels;
	}

	@Override
	public final ChannelRecyclerViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
		context = parent.getContext();
		final View channelElement = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_element, parent, false);
		return new ChannelRecyclerViewHolder(channelElement);
	}

	@Override
	public final void onBindViewHolder(final ChannelRecyclerViewHolder holder, final int position) {
		final Channel bindChannel = channels.get(position);

		holder.getTitleChannel().setText(bindChannel.getTitle());
		holder.getSubtitleChannel().setText(bindChannel.getSubtitle());
        if (null != bindChannel.getImage()) {
            holder.getImageChannel().setImageBitmap(bindChannel.getImage());
        } else {
            holder.getImageChannel().setImageResource(R.drawable.ic_rss_feed_amber_50_36dp);
        }

		if (!bindChannel.isRead()) {
			holder.getTitleChannel().setTypeface(null, Typeface.BOLD);
		}

		holder.itemView.setOnClickListener (new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (!channels.get(holder.getAdapterPosition()).isRead()) {
					final Intent intent = new Intent(context, RssChannelIntentService.class);
					intent.setAction(RssChannelIntentService.getReadCurrentChannelKey());
					intent.putExtra(RssChannelIntentService.getReadCurrentChannelKey()
                            , channels.get(holder.getAdapterPosition()));
					context.startService(intent);
				}
				final Intent intentBrowser = new Intent(context, ChannelItemActivity.class);
				intentBrowser.putExtra(ChannelItem.getKEY(), channels.get(holder.getAdapterPosition()).getLink());
				intentBrowser.putExtra(ChannelItemActivity.getChannelItemIdPreferencesKey()
						, channels.get(holder.getAdapterPosition()).getLink());
				context.startActivity(intentBrowser);
			}
		});
	}

	@Override
	public final int getItemCount() {
		return channels == null ? 0 : channels.size();
	}

	void removeItem(final int index) {
		channels.remove(index);
	}
}
