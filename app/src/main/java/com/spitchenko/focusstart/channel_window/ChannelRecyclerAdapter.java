package com.spitchenko.focusstart.channel_window;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.channel_iItem_window.ChannelItemActivity;
import com.spitchenko.focusstart.model.ChannelItem;

/**
 * Date: 24.02.17
 * Time: 0:18
 *
 * @author anatoliy
 */
final class ChannelRecyclerAdapter extends RecyclerView.Adapter<ChannelRecyclerViewHolder> {
	private ArrayList<Channel> channels;
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
		holder.getImageChannel().setImageBitmap(loadBitmapFromUrl(bindChannel.getImage()));
		if (!bindChannel.isRead()) {
			holder.getTitleChannel().setTypeface(null, Typeface.BOLD);
		}

		holder.itemView.setOnClickListener (new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!channels.get(holder.getAdapterPosition()).isRead()) {
					final Intent intent = new Intent(context, RssChannelReadIntentService.class);
					intent.putExtra(Channel.getKEY(), channels.get(holder.getAdapterPosition()));
					context.startService(intent);
				}
				final Intent intentBrowser = new Intent(context, ChannelItemActivity.class);
				intentBrowser.putExtra(ChannelItem.getKEY(), channels.get(holder.getAdapterPosition()).getLink());
				context.startActivity(intentBrowser);
			}
		});
	}

	@Override
	public final int getItemCount() {
		return channels == null ? 0 : channels.size();
	}

	private Bitmap loadBitmapFromUrl(final URL url) {
		Bitmap bmp = null;
		int SDK_INT = android.os.Build.VERSION.SDK_INT;
		if (SDK_INT > Build.VERSION_CODES.FROYO)
		{
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
			try {
				bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
			} catch (final NullPointerException e) {
				Log.d("Bitmap", "null");
			} catch (final IOException e) {
				e.printStackTrace();
			}

		}
		return bmp;
	}
}
