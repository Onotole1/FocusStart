package com.spitchenko.focusstart.utils.parser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.model.ChannelItem;
import com.spitchenko.focusstart.utils.logger.LogCatHandler;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import lombok.NonNull;

import static com.spitchenko.focusstart.utils.parser.AtomTagEnum.AtomTags;
import static com.spitchenko.focusstart.utils.parser.RssTagEnum.RssTagEnumeration;

/**
 * Date: 26.02.17
 * Time: 16:56
 *
 * @author anatoliy
 */
public final class AtomRssParser {
	public Channel parseXml(@NonNull final String url) throws IOException, XmlPullParserException {
		Channel newsModule = null;

		final XmlParser xmlParser = new XmlParser(url);
		final Tag root = xmlParser.readTags();
		if (root.getName().equals(RssTagEnumeration.RSS.text)) {
			newsModule = parseRss(xmlParser, root, url);
		} else if (root.getName().equals(AtomTags.FEED.text)) {
			newsModule = parseAtom(xmlParser, root, url);
		}
		return newsModule;
	}

	private Channel parseRss(@NonNull final XmlParser xmlParser, @NonNull final Tag root
            , @NonNull final String url) {
		final Channel singleChannel = new Channel();

		try {
			singleChannel.setTitle(xmlParser.getValueTag(RssTagEnumeration.TITLE.text
                    , RssTagEnumeration.CHANNEL.text, root));
			singleChannel.setSubtitle(xmlParser.getValueTag(RssTagEnumeration.DESCRIPTION.text
                    , RssTagEnumeration.CHANNEL.text, root));
			singleChannel.setLastBuildDate(parseAtomRssDate(xmlParser.getValueTag(RssTagEnumeration
                            .LAST_BUILD_DATE.text
					, RssTagEnumeration.CHANNEL.text, root), RssTagEnumeration.DATE_PATTERN.text));
		} catch (final NullPointerException e) {
            LogCatHandler.publishInfoRecord(e.getMessage());
		}
		singleChannel.setLink(url);

        try {
            final URL imageUrl = new URL(xmlParser.getValueTag(RssTagEnumeration.URL_RSS.text
                    , RssTagEnumeration.IMAGE.text, root));
            final Bitmap bitmap = BitmapFactory.decodeStream((InputStream)imageUrl.getContent());
            singleChannel.setImage(bitmap);
        } catch (final IOException e) {
            LogCatHandler.publishInfoRecord(e.getMessage());
        }

		singleChannel.setChannelItems(parseRssChannelItems(root, xmlParser));

		return singleChannel;
	}

	private ArrayList<ChannelItem> parseRssChannelItems(@NonNull final Tag tag
            , @NonNull final XmlParser xmlParser) {
		final ArrayList<ChannelItem> channelItems = new ArrayList<>();

        final Tag channel = xmlParser.getCurrentTagByParent(RssTagEnumeration.CHANNEL.text, tag);

		if (null != channel) {
			if (null != channel.getChildren()) {
				for (final Tag channelChildren : channel.getChildren()) {
					ChannelItem channelItem = null;
					if (channelChildren.getName().equals(RssTagEnumeration.ITEM.text)) {
						channelItem = new ChannelItem();
						for (final Tag childrenOfChannelChildren : channelChildren.getChildren()) {
                            final String childrenOfChannelChildrenName
                                    = childrenOfChannelChildren.getName();
							if (childrenOfChannelChildrenName.equals(RssTagEnumeration
                                    .TITLE.text)) {
								channelItem.setTitle(childrenOfChannelChildren.getText());
							} else if (childrenOfChannelChildrenName.equals(RssTagEnumeration
                                    .LINK.text)) {
								channelItem.setLink(childrenOfChannelChildren.getText());
							} else if (childrenOfChannelChildrenName.equals(RssTagEnumeration
                                    .DESCRIPTION.text)) {
								channelItem.setSubtitle(childrenOfChannelChildren.getText());
							} else if (childrenOfChannelChildrenName.equals(RssTagEnumeration
                                    .PUB_DATE.text)) {
								channelItem.setPubDate(parseAtomRssDate(childrenOfChannelChildren
                                                .getText()
                                        , RssTagEnumeration.DATE_PATTERN.text));
							}
						}
					}
					if (null != channelItem) {
						channelItems.add(channelItem);
					}
				}
			}
		}
		return channelItems;
	}

	private Date parseAtomRssDate(@NonNull final String input, @NonNull final String pattern) {
		final SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
		try {
			return format.parse(input);
		} catch (final ParseException e) {
            LogCatHandler.publishInfoRecord(e.getMessage());
			return null;
		} catch (final NullPointerException e) {
            LogCatHandler.publishInfoRecord(e.getMessage());
			return null;
		}
	}

	private Channel parseAtom(@NonNull final XmlParser xmlParser, @NonNull final Tag root
            , @NonNull final String url) {
		final Channel singleChannel = new Channel();

		singleChannel.setTitle(xmlParser.getValueTag(AtomTags.TITLE.text, AtomTags.FEED.text, root));
		singleChannel.setSubtitle(xmlParser.getValueTag(AtomTags.SUBTITLE.text, AtomTags.FEED.text
                , root));
		singleChannel.setLastBuildDate(parseAtomRssDate(xmlParser.getValueTag(AtomTags.UPDATED.text
				, AtomTags.FEED.text, root), AtomTags.DATE_PATTERN.text));

		singleChannel.setLink(url);

        try {
            final URL imageUrl = new URL(xmlParser.getValueTag(AtomTags.ICON.text
                    , AtomTags.ICON.text, root));
            final Bitmap bitmap = BitmapFactory.decodeStream((InputStream)imageUrl.getContent());
            singleChannel.setImage(bitmap);
        } catch (final IOException e) {
            LogCatHandler.publishInfoRecord(e.getMessage());
        }

		singleChannel.setChannelItems(parseAtomChannelItems(root, xmlParser));

		return singleChannel;
	}

	private ArrayList<ChannelItem> parseAtomChannelItems(@NonNull final Tag tag
            , @NonNull final XmlParser xmlParser) {
		final ArrayList<ChannelItem> channelItems = new ArrayList<>();

		final Tag channel = xmlParser.getCurrentTagByParent(AtomTags.FEED.text, tag);

        if (null != channel) {
            for (final Tag channelChildren : channel.getChildren()) {
                ChannelItem channelItem = null;
                if (channelChildren.getName().equals(AtomTags.ENTRY.text)) {
                    channelItem = new ChannelItem();
                    for (final Tag childrenOfChannelChildren : channelChildren.getChildren()) {
                        final String childrenOfChannelChildrenName
                                = childrenOfChannelChildren.getName();
                        if (childrenOfChannelChildrenName.equals(AtomTags.TITLE.text)) {
                            channelItem.setTitle(childrenOfChannelChildren.getText());
                        } else if (childrenOfChannelChildrenName.equals(AtomTags.LINK.text)) {
                            channelItem.setLink(childrenOfChannelChildren.getAttributes().get(
                                    AtomTags.LINK_HREF.text));
                        } else if (childrenOfChannelChildrenName.equals(AtomTags.PUBLISHED.text)) {
                            channelItem.setSubtitle(childrenOfChannelChildren.getText());
                        } else if (childrenOfChannelChildrenName.equals(AtomTags.CONTENT.text)) {
                            channelItem.setSubtitle(childrenOfChannelChildren.getText());
                        } else if (childrenOfChannelChildrenName.equals(AtomTags.UPDATED.text)) {
                            channelItem.setPubDate(parseAtomRssDate(childrenOfChannelChildren
                                            .getText(), AtomTags.DATE_PATTERN.text));
                        }
                    }
                }
                if (null != channelItem) {
                    channelItems.add(channelItem);
                }
            }
        }
		return channelItems;
	}
}
