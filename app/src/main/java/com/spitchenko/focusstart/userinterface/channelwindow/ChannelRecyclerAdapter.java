package com.spitchenko.focusstart.userinterface.channelwindow;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.controller.channelwindow.RssChannelIntentService;
import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.userinterface.channellitem.ChannelItemActivity;

import java.util.ArrayList;

import lombok.Getter;

import static com.spitchenko.focusstart.controller.channelitemwindow.ChannelItemActivityAndBroadcastObserver.getPrefsUrlKey;

/**
 * Date: 24.02.17
 * Time: 0:18
 *
 * @author anatoliy
 */
public final class ChannelRecyclerAdapter extends RecyclerView.Adapter<ChannelRecyclerViewHolder> {
	private final @Getter ArrayList<Channel> channels;
	private Context context;

	public ChannelRecyclerAdapter(final ArrayList<Channel> channels) {
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
                    RssChannelIntentService.start(channels.get(holder.getAdapterPosition())
                            , RssChannelIntentService.getReadCurrentChannelKey(), context);
				}
				ChannelItemActivity.start(getPrefsUrlKey()
                        , channels.get(holder.getAdapterPosition()).getLink(), context);
			}
		});
	}

	@Override
	public final int getItemCount() {
		return channels == null ? 0 : channels.size();
	}

	public void removeItem(final int index) {
		channels.remove(index);
	}
}
