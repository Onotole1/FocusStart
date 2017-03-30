package com.spitchenko.focusstart.userinterface.channellitem;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.controller.RssChannelItemIntentService;
import com.spitchenko.focusstart.model.ChannelItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import lombok.NonNull;

/**
 * Date: 24.02.17
 * Time: 0:18
 *
 * @author anatoliy
 */
final class ChannelItemRecyclerAdapter extends RecyclerView.Adapter<ChannelItemRecyclerViewHolder> {
	private final ArrayList<ChannelItem> channelItems;
	private Context context;
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.ENGLISH);

	ChannelItemRecyclerAdapter(@NonNull final ArrayList<ChannelItem> channelItems) {
		this.channelItems = channelItems;
	}

	@Override
	public final ChannelItemRecyclerViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
		context = parent.getContext();
		final View channelElement = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_item_element, parent, false);
		return new ChannelItemRecyclerViewHolder(channelElement);
	}

	@Override
	public final void onBindViewHolder(@NonNull final ChannelItemRecyclerViewHolder holder
            , final int position) {
		final ChannelItem bindChannel = channelItems.get(position);

		holder.getTitleChannel().setText(bindChannel.getTitle());
		holder.getSubtitleChannel().setText(bindChannel.getSubtitle());
        if (null != bindChannel.getUpdateDate()) {
            holder.getUpdateDate().setText(formatter.format(bindChannel.getUpdateDate()));
        } else if (null != bindChannel.getPubDate()) {
            holder.getUpdateDate().setText(formatter.format(bindChannel.getPubDate()));
        }
		if (!bindChannel.isRead()) {
			holder.getTitleChannel().setTypeface(null, Typeface.BOLD);
		}

		holder.itemView.setOnClickListener (new View.OnClickListener() {
			@Override
			public void onClick(@NonNull final View v) {
				if (!channelItems.get(holder.getAdapterPosition()).isRead()) {
					final Intent intent = new Intent(context, RssChannelItemIntentService.class);
					intent.setAction(RssChannelItemIntentService.getReadCurrentChannelKey());
					intent.putExtra(ChannelItem.getKEY(), channelItems.get(holder.getAdapterPosition()));
					context.startService(intent);
				}
				final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(channelItems.get(holder.getAdapterPosition()).getLink()));
				context.startActivity(intent);
			}
		});
	}

	@Override
	public final int getItemCount() {
		return channelItems == null ? 0 : channelItems.size();
	}
}
