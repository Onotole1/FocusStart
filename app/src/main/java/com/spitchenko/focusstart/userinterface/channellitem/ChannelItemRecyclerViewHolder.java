package com.spitchenko.focusstart.userinterface.channellitem;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.spitchenko.focusstart.R;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Date: 24.02.17
 * Time: 0:25
 *
 * @author anatoliy
 */
final class ChannelItemRecyclerViewHolder extends RecyclerView.ViewHolder {

	private @Getter @Setter	TextView titleChannel;
	private @Getter @Setter WebView subtitleChannel;
    private @Getter @Setter	TextView updateDate;

	ChannelItemRecyclerViewHolder(@NonNull final View itemView) {
		super(itemView);
		titleChannel = (TextView)itemView.findViewById(R.id.channel_item_element_tittle_text_view);
		subtitleChannel = (WebView) itemView.findViewById(R.id.channel_element_web_view);
        updateDate = (TextView)itemView.findViewById(R.id.channel_item_element_update_text_view);
	}
}
