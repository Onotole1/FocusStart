package com.spitchenko.focusstart.channel_window;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.spitchenko.focusstart.model.ChannelItem;

/**
 * Date: 26.02.17
 * Time: 16:56
 *
 * @author anatoliy
 */
final class AtomRssParser {
	Channel parseXml(final URL url) {
		Channel newsModule = null;

		XmlParser xmlParser = new XmlParser(url);
		Tag root = xmlParser.readTags();
		if (root.getName().equals(RssTagEnum.rssTag.RSS.text)) {
			newsModule = parseRss(xmlParser, root, url);
		} else if (root.getName().equals(AtomTagEnum.atomTag.FEED.text)) {
			newsModule = parseAtom(xmlParser, root, url);
		}
		return newsModule;
	}

	private Channel parseRss(final XmlParser xmlParser, final Tag root, final URL url) {
		final Channel singleChannel = new Channel();

		singleChannel.setTitle(xmlParser.getValueTag(RssTagEnum.rssTag.TITLE.text, RssTagEnum.rssTag.CHANNEL.text, root));
		singleChannel.setSubtitle(xmlParser.getValueTag(RssTagEnum.rssTag.DESCRIPTION.text, RssTagEnum.rssTag.CHANNEL.text, root));
		singleChannel.setLastBuildDate(parseAtomRssDate(xmlParser.getValueTag(RssTagEnum.rssTag.LAST_BUILD_DATE.text
				, RssTagEnum.rssTag.CHANNEL.text, root), RssTagEnum.rssTag.DATE_PATTERN.text));
		try {
			singleChannel.setLink(url);
			singleChannel.setImage(new URL(xmlParser.getValueTag(RssTagEnum.rssTag.URL_RSS.text, RssTagEnum.rssTag.IMAGE.text, root)));
		} catch (final MalformedURLException e) {
			e.printStackTrace();
		}
		singleChannel.setChannelItems(parseRssChannelItems(root));

		return singleChannel;
	}

	private ArrayList<ChannelItem> parseRssChannelItems(final Tag tag) {
		ArrayList<ChannelItem> channelItems = new ArrayList<>();

		if (null != tag) {
			if (null != tag.getChildren()) {
				for (final Tag t : tag.getChildren()) {
					ChannelItem channelItem = null;
					if (t.getName().equals(RssTagEnum.rssTag.ITEM.text)) {
						channelItem = new ChannelItem();
						for (final Tag t1 : t.getChildren()) {
							if (t1.getName().equals(RssTagEnum.rssTag.TITLE.text)) {
								channelItem.setTitle(t1.getText());
							} else if (t1.getName().equals(RssTagEnum.rssTag.LINK.text)) {
								try {
									channelItem.setLink(new URL(t1.getText()));
								} catch (final MalformedURLException e) {
									e.printStackTrace();
								}
							} else if (t1.getName().equals(RssTagEnum.rssTag.DESCRIPTION.text)) {
								channelItem.setSubtitle(t1.getText());
							} else if (t1.getName().equals(RssTagEnum.rssTag.PUB_DATE.text)) {
								channelItem.setPubDate(t1.getText());
							}
						}
					}
					if (null != channelItem) {
						channelItems.add(channelItem);
					}
				}
				if (channelItems.isEmpty()) {
					for (final Tag t : tag.getChildren()) {
						channelItems = parseRssChannelItems(t);
					}
				}
			}
		}
		return channelItems;
	}

	private Date parseAtomRssDate(final String input, final String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
		try {
			return format.parse(input);
		} catch (final ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Channel parseAtom(final XmlParser xmlParser, final Tag root, final URL url) {
		final Channel singleChannel = new Channel();

		singleChannel.setTitle(xmlParser.getValueTag(AtomTagEnum.atomTag.TITLE.text, AtomTagEnum.atomTag.FEED.text, root));
		singleChannel.setSubtitle(xmlParser.getValueTag(AtomTagEnum.atomTag.SUBTITLE.text, AtomTagEnum.atomTag.FEED.text, root));
		singleChannel.setLastBuildDate(parseAtomRssDate(xmlParser.getValueTag(AtomTagEnum.atomTag.UPDATED.text
				, AtomTagEnum.atomTag.FEED.text, root), AtomTagEnum.atomTag.DATE_PATTERN.text));
		try {
			singleChannel.setLink(url);
			singleChannel.setImage(new URL(xmlParser.getValueTag(AtomTagEnum.atomTag.FEED.text, AtomTagEnum.atomTag.ICON.text, root)));
		} catch (final MalformedURLException e) {
			e.printStackTrace();
		}
		singleChannel.setChannelItems(parseAtomChannelItems(root, xmlParser));

		return singleChannel;
	}

	private ArrayList<ChannelItem> parseAtomChannelItems(final Tag tag, final XmlParser xmlParser) {
		final ArrayList<ChannelItem> channelItems = new ArrayList<>();

		Tag channel = xmlParser.getCurrentTagByParent(AtomTagEnum.atomTag.FEED.text, tag);

		for (final Tag t:channel.getChildren()) {
			ChannelItem channelItem = null;
			if (t.getName().equals(AtomTagEnum.atomTag.ENTRY.text)) {
				channelItem = new ChannelItem();
				for (final Tag t1:t.getChildren()) {
					if (t1.getName().equals(AtomTagEnum.atomTag.TITLE.text)) {
						channelItem.setTitle(t1.getText());
					} else if (t1.getName().equals(AtomTagEnum.atomTag.LINK.text)) {
						try {
							channelItem.setLink(new URL(t1.getAttributes().get(AtomTagEnum.atomTag.LINK_HREF.text)));
						} catch (final MalformedURLException e) {
							e.printStackTrace();
						}
					} else if (t1.getName().equals(AtomTagEnum.atomTag.PUBLISHED.text)) {
						channelItem.setSubtitle(t1.getText());
					} else if (t1.getName().equals(AtomTagEnum.atomTag.UPDATED.text)) {
						channelItem.setPubDate(t1.getText());
					}
				}
			}
			if (null != channelItem) {
				channelItems.add(channelItem);
			}
		}
		return channelItems;
	}
}
