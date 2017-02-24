package com.spitchenko.focusstart.adapters;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.spitchenko.focusstart.R;
import com.spitchenko.focusstart.model.Channel;

/**
 * Date: 24.02.17
 * Time: 0:18
 *
 * @author anatoliy
 */
public final class ChannelRecyclerAdapter extends RecyclerView.Adapter<ChannelRecyclerViewHolder> {
	private List<Channel> mChannels;

	public ChannelRecyclerAdapter(final List<Channel> channels) {
		mChannels = channels;
	}

	@Override
	public final ChannelRecyclerViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
		View channelElement = LayoutInflater.from(parent.getContext()).inflate(R.layout.channel_element, parent, false);
		return new ChannelRecyclerViewHolder(channelElement);
	}

	@Override
	public final void onBindViewHolder(final ChannelRecyclerViewHolder holder, final int position) {
		final Channel bindChannel = mChannels.get(position);

		holder.getTitleChannel().setText(bindChannel.getTitle());
		holder.getSubtitleChannel().setText(bindChannel.getSubtitle());
		holder.getImageChannel().setImageBitmap(loadBitmapFromUrl(bindChannel.getImage()));
	}

	@Override
	public final int getItemCount() {
		return mChannels.size();
	}

	private Bitmap loadBitmapFromUrl(final URL url) {
		Bitmap bmp = null;
		int SDK_INT = android.os.Build.VERSION.SDK_INT;
		if (SDK_INT > 8)
		{
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
			try {
				bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return bmp;
	}
}
