package com.spitchenko.focusstart.model;

import java.net.URL;

import lombok.Getter;
import lombok.Setter;

/**
 * Date: 24.02.17
 * Time: 12:05
 *
 * @author anatoliy
 */
public final class ChannelItem {
	private @Getter @Setter String title;
	private @Getter @Setter String subtitle;
	private @Getter @Setter String pubDate;
	private @Getter @Setter URL link;
}
