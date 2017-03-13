package com.spitchenko.focusstart.channel_window;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Date: 25.02.17
 * Time: 20:36
 *
 * @author anatoliy
 */
final class XmlParser {
	private XmlPullParser xpp;

	XmlParser(final URL url) {
		try {
			final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			xpp = factory.newPullParser();
			final InputStream inputStream = getInputStream(url);
			xpp.setInput(inputStream, "UTF_8");
		} catch (final XmlPullParserException e) {
			e.printStackTrace();
		}
	}

	Tag readTags() {
		Tag parent = null;
		Tag nextTag = null;
		int depth = 0;
		try {
			int eventType = xpp.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {

				if(eventType == XmlPullParser.START_TAG) {
					nextTag = new Tag(xpp.getName(), parent, xpp.getDepth() );

					if (null == parent) {
						parent = nextTag;
					} else if (xpp.getDepth() > depth) {
						parent.children.add(nextTag);
						parent = nextTag;
					} else 	if (xpp.getDepth() < depth) {
						while (xpp.getDepth() - parent.getDepth() != 1) {
							parent = parent.parent;
						}
						nextTag.setParent(parent);
						parent.children.add(nextTag);
						parent = nextTag;
					} else if(xpp.getDepth() == depth) {
						if (parent.getDepth() == nextTag.getDepth()) {
							parent = parent.parent;
							nextTag.setParent(parent);
						}
						parent.children.add(nextTag);
						parent = nextTag;
					}
					for (int i = 0; i < xpp.getAttributeCount(); i++) {
						nextTag.getAttributes().put(xpp.getAttributeName(i), xpp.getAttributeValue(i));
					}
					depth = xpp.getDepth();
				}

				else if(eventType == XmlPullParser.TEXT && !xpp.isWhitespace() && null != nextTag) {
					if (null != xpp.getText()) {
						nextTag.setText(xpp.getText());
					}
				}

				eventType = xpp.next();
			}
		} catch (final XmlPullParserException | IOException e) {
			e.printStackTrace();
		}

		return getRoot(parent);
	}

	private InputStream getInputStream(final URL url) {
		try {
			return url.openConnection().getInputStream();//java.net.unknownhostexception unable to resolve host
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Tag getRoot(final Tag tag) {
		Tag result = tag;
		if (null != tag.getParent()) {
			result = getRoot(tag.getParent());
		}
		return result;
	}

	String getValueTag(final String inputTag, final String parentTag, final Tag tag) {
		Tag parent = getCurrentTagByParent(parentTag, tag);
		if (null != parent && null != parent.getChildren()) {
			for (Tag t : parent.getChildren()) {
				if (t.getName().equals(inputTag)) {
					return t.getText();
				}
			}
		}
		return null;
	}

	Tag getCurrentTagByParent(final String inputTag, final Tag tag) {
		Tag result = null;
		if (inputTag.equals(tag.getName())) {
			return tag;
		} else {
			for (final Tag t : tag.getChildren()) {
				if (t.getName().equals(inputTag)) {
					return t;
				}
			}
			for (final Tag t : tag.getChildren()) {
				result = getCurrentTagByParent(inputTag, t);
			}
		}
		return result;
	}
}
