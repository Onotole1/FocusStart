package com.spitchenko.focusstart.channel_iItem_window;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.model.ChannelItem;

/**
 * Date: 24.02.17
 * Time: 0:18
 *
 * @author anatoliy
 */
final class ChannelItemRecyclerAdapter extends RecyclerView.Adapter<ChannelItemRecyclerViewHolder> {
	private ArrayList<ChannelItem> channelItems;
	private Context context;

	ChannelItemRecyclerAdapter(final ArrayList<ChannelItem> channelItems) {
		this.channelItems = channelItems;
	}

	@Override
	public final ChannelItemRecyclerViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
		context = parent.getContext();
		View channelElement = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_item_element, parent, false);
		return new ChannelItemRecyclerViewHolder(channelElement);
	}

	@Override
	public final void onBindViewHolder(final ChannelItemRecyclerViewHolder holder, final int position) {
		final ChannelItem bindChannel = channelItems.get(position);

		holder.getTitleChannel().setText(bindChannel.getTitle());
		holder.getSubtitleChannel().setText(bindChannel.getSubtitle());
		if (!bindChannel.isRead()) {
			holder.getTitleChannel().setTypeface(null, Typeface.BOLD);
		}

		holder.itemView.setOnClickListener (new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!channelItems.get(holder.getAdapterPosition()).isRead()) {
					Intent intent = new Intent(context, RssChannelItemReadIntentService.class);
					intent.putExtra(ChannelItem.getKEY(), channelItems.get(holder.getAdapterPosition()));
					context.startService(intent);
				}
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(channelItems.get(holder.getAdapterPosition()).getLink().toString()));
				context.startActivity(intent);
			}
		});
	}

	@Override
	public final int getItemCount() {
		return channelItems == null ? 0 : channelItems.size();
	}
}
