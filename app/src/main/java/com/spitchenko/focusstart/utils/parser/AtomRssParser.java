package com.spitchenko.focusstart.utils.parser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.model.ChannelItem;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Date: 26.02.17
 * Time: 16:56
 *
 * @author anatoliy
 */
@SuppressWarnings( "deprecation" )
public final class AtomRssParser {
	public Channel parseXml(final String url) throws IOException, XmlPullParserException {
		Channel newsModule = null;

		final XmlParser xmlParser = new XmlParser(url);
		final Tag root = xmlParser.readTags();
		if (root.getName().equals(RssTagEnum.RssTagEnumeration.RSS.text)) {
			newsModule = parseRss(xmlParser, root, url);
		} else if (root.getName().equals(AtomTagEnum.AtomTags.FEED.text)) {
			newsModule = parseAtom(xmlParser, root, url);
		}
		return newsModule;
	}

	private Channel parseRss(final XmlParser xmlParser, final Tag root, final String url) {
		final Channel singleChannel = new Channel();

		try {
			singleChannel.setTitle(xmlParser.getValueTag(RssTagEnum.RssTagEnumeration.TITLE.text, RssTagEnum.RssTagEnumeration.CHANNEL.text, root));
			singleChannel.setSubtitle(xmlParser.getValueTag(RssTagEnum.RssTagEnumeration.DESCRIPTION.text, RssTagEnum.RssTagEnumeration.CHANNEL.text, root));
			singleChannel.setLastBuildDate(parseAtomRssDate(xmlParser.getValueTag(RssTagEnum.RssTagEnumeration.LAST_BUILD_DATE.text
					, RssTagEnum.RssTagEnumeration.CHANNEL.text, root), RssTagEnum.RssTagEnumeration.DATE_PATTERN.text));
		} catch (final NullPointerException e) {
			e.printStackTrace();
		}
		singleChannel.setLink(url);

        try {
            final URL imageUrl = new URL(xmlParser.getValueTag(RssTagEnum.RssTagEnumeration.URL_RSS.text, RssTagEnum.RssTagEnumeration.IMAGE.text, root));
            final Bitmap bitmap = BitmapFactory.decodeStream((InputStream)imageUrl.getContent());
            singleChannel.setImage(bitmap);
        } catch (final IOException e) {
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
					if (t.getName().equals(RssTagEnum.RssTagEnumeration.ITEM.text)) {
						channelItem = new ChannelItem();
						for (final Tag t1 : t.getChildren()) {
							if (t1.getName().equals(RssTagEnum.RssTagEnumeration.TITLE.text)) {
								channelItem.setTitle(t1.getText());
							} else if (t1.getName().equals(RssTagEnum.RssTagEnumeration.LINK.text)) {
								channelItem.setLink(t1.getText());
							} else if (t1.getName().equals(RssTagEnum.RssTagEnumeration.DESCRIPTION.text)) {
								channelItem.setSubtitle(t1.getText());
							} else if (t1.getName().equals(RssTagEnum.RssTagEnumeration.PUB_DATE.text)) {
								channelItem.setPubDate(new Date(t1.getText()));
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
		final SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
		try {
			return format.parse(input);
		} catch (final ParseException e) {
			e.printStackTrace();
			return null;
		} catch (final NullPointerException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Channel parseAtom(final XmlParser xmlParser, final Tag root, final String url) {
		final Channel singleChannel = new Channel();

		singleChannel.setTitle(xmlParser.getValueTag(AtomTagEnum.AtomTags.TITLE.text, AtomTagEnum.AtomTags.FEED.text, root));
		singleChannel.setSubtitle(xmlParser.getValueTag(AtomTagEnum.AtomTags.SUBTITLE.text, AtomTagEnum.AtomTags.FEED.text, root));
		singleChannel.setLastBuildDate(parseAtomRssDate(xmlParser.getValueTag(AtomTagEnum.AtomTags.UPDATED.text
				, AtomTagEnum.AtomTags.FEED.text, root), AtomTagEnum.AtomTags.DATE_PATTERN.text));

		singleChannel.setLink(url);

        try {
            final URL imageUrl = new URL(xmlParser.getValueTag(RssTagEnum.RssTagEnumeration.URL_RSS.text, RssTagEnum.RssTagEnumeration.IMAGE.text, root));
            final Bitmap bitmap = BitmapFactory.decodeStream((InputStream)imageUrl.getContent());
            singleChannel.setImage(bitmap);
        } catch (final IOException e) {
            e.printStackTrace();
        }

		singleChannel.setChannelItems(parseAtomChannelItems(root, xmlParser));

		return singleChannel;
	}

	private ArrayList<ChannelItem> parseAtomChannelItems(final Tag tag, final XmlParser xmlParser) {
		final ArrayList<ChannelItem> channelItems = new ArrayList<>();

		final Tag channel = xmlParser.getCurrentTagByParent(AtomTagEnum.AtomTags.FEED.text, tag);

		for (final Tag t:channel.getChildren()) {
			ChannelItem channelItem = null;
			if (t.getName().equals(AtomTagEnum.AtomTags.ENTRY.text)) {
				channelItem = new ChannelItem();
				for (final Tag t1:t.getChildren()) {
					if (t1.getName().equals(AtomTagEnum.AtomTags.TITLE.text)) {
						channelItem.setTitle(t1.getText());
					} else if (t1.getName().equals(AtomTagEnum.AtomTags.LINK.text)) {
						channelItem.setLink(t1.getAttributes().get(AtomTagEnum.AtomTags.LINK_HREF.text));
					} else if (t1.getName().equals(AtomTagEnum.AtomTags.PUBLISHED.text)) {
						channelItem.setSubtitle(t1.getText());
					} else if (t1.getName().equals(AtomTagEnum.AtomTags.UPDATED.text)) {
						channelItem.setPubDate(parseAtomRssDate(t1.getText(), AtomTagEnum.AtomTags.DATE_PATTERN.text));
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
