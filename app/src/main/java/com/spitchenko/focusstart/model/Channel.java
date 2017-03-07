package com.spitchenko.focusstart.model;

import java.net.URL;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Date: 24.02.17
 * Time: 12:05
 *
 * @author anatoliy
 */
public final class Channel implements NewsModule {
	private @Getter @Setter	String title;
	private @Getter @Setter String subtitle;
	private @Getter @Setter String lastBuildDate;
	private @Getter @Setter URL link;
	private @Getter @Setter URL image;
	private @Getter @Setter List<ChannelItem> channelItems;
}
