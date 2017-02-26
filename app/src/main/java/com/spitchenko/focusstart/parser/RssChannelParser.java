package com.spitchenko.focusstart.parser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.spitchenko.focusstart.model.Channel;
import com.spitchenko.focusstart.model.NewsModule;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Date: 26.02.17
 * Time: 16:56
 *
 * @author anatoliy
 */
public class RssChannelParser extends XmlParser {
	@Override
	public NewsModule parseXml(final URL url) {
		String title = null;
		String subtitle = null;
		String lastBuildDate = null;
		URL link = null;
		URL image = null;

		Element root = null;
		Document document = null;
		try {
			document = dBuilder.parse(getInputStream(url));
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}
		if (document != null) {
			root = document.getDocumentElement();
		}

		if (root != null) {
			final NodeList nodeListChannel = root.getElementsByTagName(RssTag.rssTag.CHANNEL.text);
			final Node nodeChannel = getNodeByTag(nodeListChannel, RssTag.rssTag.CHANNEL.text);
			title = getStringFromNode(nodeChannel, RssTag.rssTag.TITLE.text);
			subtitle = getStringFromNode(nodeChannel, RssTag.rssTag.DESCRIPTION.text);
			lastBuildDate = getStringFromNode(nodeChannel, RssTag.rssTag.LAST_BUILD_DATE.text);
			try {
				link = new URL(getStringFromNode(nodeChannel, RssTag.rssTag.LINK.text));
				final Node nodeImage = getNodeByTag(nodeChannel.getChildNodes(), RssTag.rssTag.IMAGE.text);
				if (nodeImage != null) {
					image = new URL(getStringFromNode(nodeImage, RssTag.rssTag.URL_RSS.text));
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}

		Channel singleChannel = new Channel();
		singleChannel.setTitle(title);
		singleChannel.setSubtitle(subtitle);
		singleChannel.setLastBuildDate(lastBuildDate);
		singleChannel.setLink(link);
		singleChannel.setImage(image);

		return singleChannel;
	}
}
