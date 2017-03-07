package com.spitchenko.focusstart.parser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.model.ChannelItem;
import com.spitchenko.focusstart.model.NewsModule;

/**
 * Date: 26.02.17
 * Time: 16:56
 *
 * @author anatoliy
 */
public class AtomRssParser {
	public NewsModule parseXml(final URL url) {
		NewsModule newsModule = null;

		XmlParser xmlParser = new XmlParser(url);
		Tag root = xmlParser.readTags();
		if (root.getName().equals(RssTagEnum.rssTag.RSS.text)) {
			newsModule = parseRss(xmlParser, root);
		} else if (root.getName().equals(AtomTagEnum.atomTag.FEED.text)) {
			newsModule = parseAtom(xmlParser, root);
		}
		return newsModule;
	}

	private Channel parseRss(final XmlParser xmlParser, final Tag root) {
		Channel singleChannel = new Channel();

		singleChannel.setTitle(xmlParser.getValueTag(RssTagEnum.rssTag.TITLE.text, RssTagEnum.rssTag.CHANNEL.text, root));
		singleChannel.setSubtitle(xmlParser.getValueTag(RssTagEnum.rssTag.DESCRIPTION.text, RssTagEnum.rssTag.CHANNEL.text, root));
		singleChannel.setLastBuildDate(xmlParser.getValueTag(RssTagEnum.rssTag.LAST_BUILD_DATE.text, RssTagEnum.rssTag.CHANNEL.text, root));
		try {
			singleChannel.setLink(new URL(xmlParser.getValueTag(RssTagEnum.rssTag.LINK.text, RssTagEnum.rssTag.CHANNEL.text, root)));
			singleChannel.setImage(new URL(xmlParser.getValueTag(RssTagEnum.rssTag.URL_RSS.text, RssTagEnum.rssTag.IMAGE.text, root)));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		singleChannel.setChannelItems(parseRssChannelItems(root, xmlParser));

		return singleChannel;
	}

	private List<ChannelItem> parseRssChannelItems(final Tag tag, final XmlParser xmlParser) {
		List<ChannelItem> channelItems = new ArrayList<>();

		Tag channel = xmlParser.getCurrentTagByParent(RssTagEnum.rssTag.RSS.text, tag);

		for (Tag t:channel.getChildren()) {
			ChannelItem channelItem = null;
			if (t.getName().equals(RssTagEnum.rssTag.ITEM.text)) {
				channelItem = new ChannelItem();
				for (Tag t1:t.getChildren()) {
					if (t1.getName().equals(RssTagEnum.rssTag.TITLE.text)) {
						channelItem.setTitle(t1.getText());
					} else if (t1.getName().equals(RssTagEnum.rssTag.LINK.text)) {
						try {
							channelItem.setLink(new URL(t1.getText()));
						} catch (MalformedURLException e) {
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
		return channelItems;
	}

	private Channel parseAtom(final XmlParser xmlParser, final Tag root) {
		Channel singleChannel = new Channel();

		singleChannel.setTitle(xmlParser.getValueTag(AtomTagEnum.atomTag.TITLE.text, AtomTagEnum.atomTag.FEED.text, root));
		singleChannel.setSubtitle(xmlParser.getValueTag(AtomTagEnum.atomTag.SUBTITLE.text, AtomTagEnum.atomTag.FEED.text, root));
		singleChannel.setLastBuildDate(xmlParser.getValueTag(AtomTagEnum.atomTag.UPDATED.text, AtomTagEnum.atomTag.FEED.text, root));
		try {
			singleChannel.setLink(new URL(xmlParser.getValueAttributeTag(AtomTagEnum.atomTag.LINK.text
					, AtomTagEnum.atomTag.FEED.text, AtomTagEnum.atomTag.LINK_HREF.text, root)));
			singleChannel.setImage(new URL(xmlParser.getValueTag(AtomTagEnum.atomTag.FEED.text, AtomTagEnum.atomTag.ICON.text, root)));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		singleChannel.setChannelItems(parseAtomChannelItems(root, xmlParser));

		return singleChannel;
	}

	private List<ChannelItem> parseAtomChannelItems(final Tag tag, final XmlParser xmlParser) {
		List<ChannelItem> channelItems = new ArrayList<>();

		Tag channel = xmlParser.getCurrentTagByParent(AtomTagEnum.atomTag.FEED.text, tag);

		for (Tag t:channel.getChildren()) {
			ChannelItem channelItem = null;
			if (t.getName().equals(AtomTagEnum.atomTag.ENTRY.text)) {
				channelItem = new ChannelItem();
				for (Tag t1:t.getChildren()) {
					if (t1.getName().equals(AtomTagEnum.atomTag.TITLE.text)) {
						channelItem.setTitle(t1.getText());
					} else if (t1.getName().equals(AtomTagEnum.atomTag.LINK.text)) {
						try {
							channelItem.setLink(new URL(t1.getAttributes().get(AtomTagEnum.atomTag.LINK_HREF.text)));
						} catch (MalformedURLException e) {
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
