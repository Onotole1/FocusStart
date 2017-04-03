package com.spitchenko.focusstart.userinterface.channellitem;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.controller.channelitemwindow.RssChannelItemIntentService;
import com.spitchenko.focusstart.model.ChannelItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import lombok.NonNull;

/**
 * Date: 24.02.17
 * Time: 0:18
 *
 * @author anatoliy
 */
public final class ChannelItemRecyclerAdapter extends RecyclerView.Adapter<ChannelItemRecyclerViewHolder> {
	private final ArrayList<ChannelItem> channelItems;
	private Context context;
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.ENGLISH);

	public ChannelItemRecyclerAdapter(@NonNull final ArrayList<ChannelItem> channelItems) {
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
        final String data = "<html><body style='margin:0;padding:0;'>" + bindChannel.getSubtitle() + "</body></html>";
        holder.getSubtitleChannel().loadDataWithBaseURL("", data
                , "text/html", Xml.Encoding.UTF_8.toString(), null);
        if (null != bindChannel.getPubDate()) {
            final long edtTime = bindChannel.getPubDate().getTime();
            final long timezoneAlteredTime = edtTime + Calendar.getInstance().getTimeZone().getRawOffset();
            final Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timezoneAlteredTime);

            holder.getUpdateDate().setText(formatter.format(calendar.getTime()));
        }
		if (!bindChannel.isRead()) {
			holder.getTitleChannel().setTypeface(null, Typeface.BOLD);
		}

		holder.itemView.setOnClickListener (new View.OnClickListener() {
			@Override
			public void onClick(@NonNull final View v) {
				if (!channelItems.get(holder.getAdapterPosition()).isRead()) {
                    RssChannelItemIntentService.start(channelItems.get(holder.getAdapterPosition())
                            , null
                            , RssChannelItemIntentService.getReadCurrentChannelKey(), context);
				}
				final Intent intent = new Intent(Intent.ACTION_VIEW
                        , Uri.parse(channelItems.get(holder.getAdapterPosition()).getLink()));
				context.startActivity(intent);
			}
		});

        holder.getSubtitleChannel().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        if (!channelItems.get(holder.getAdapterPosition()).isRead()) {
                            RssChannelItemIntentService.start(channelItems.get(holder.getAdapterPosition())
                                    , null
                                    , RssChannelItemIntentService.getReadCurrentChannelKey(), context);
                        }
                        final Intent intent = new Intent(Intent.ACTION_VIEW
                                , Uri.parse(channelItems.get(holder.getAdapterPosition()).getLink()));
                        context.startActivity(intent);
                        break;
                }
                return false;
            }
        });
	}

	@Override
	public final int getItemCount() {
		return channelItems == null ? 0 : channelItems.size();
	}
}
