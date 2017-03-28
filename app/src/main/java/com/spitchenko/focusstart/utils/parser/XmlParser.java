package com.spitchenko.focusstart.utils.parser;

import com.spitchenko.focusstart.utils.logger.LogCatHandler;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import lombok.NonNull;

/**
 * Date: 25.02.17
 * Time: 20:36
 *
 * @author anatoliy
 */
final class XmlParser {
	private XmlPullParser xpp;

	XmlParser(final String url) throws IOException, XmlPullParserException {
        final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        xpp = factory.newPullParser();
        final InputStream inputStream = getInputStream(new URL(url));
        if (null != inputStream) {
            xpp.setInput(inputStream, null);
        } else {
            throw new IOException();
        }
	}

	Tag readTags() {
		Tag parent = null;
		Tag nextTag = null;
		int depth = 0;
		try {
			int eventType = xpp.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {

				if(eventType == XmlPullParser.START_TAG) {//Проверка на тип
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
				} else if((eventType == XmlPullParser.TEXT) && !xpp.isWhitespace() && null != nextTag) {
					if (null != xpp.getText()) {
						nextTag.setText(xpp.getText());
					}
				} else if (eventType == XmlPullParser.CDSECT && null != xpp.getText() && null != nextTag) {
					nextTag.setText(xpp.getText());
				}

				eventType = xpp.nextToken();
			}
		} catch (final XmlPullParserException | IOException e) {
            LogCatHandler.publishInfoRecord(e.getMessage());
		}

		return getRoot(parent);
	}

	private InputStream getInputStream(final URL url) {
		try {
			return url.openConnection().getInputStream();
		} catch (final IOException e) {
            LogCatHandler.publishInfoRecord(e.getMessage());
			return null;
		}
	}

	private Tag getRoot(@NonNull final Tag tag) {
		Tag result = tag;
		if (null != tag.getParent()) {
			result = getRoot(tag.getParent());
		}
		return result;
	}

	String getValueTag(final String inputTag, final String parentTag, final Tag tag) {
		final Tag parent = getCurrentTagByParent(parentTag, tag);
		if (null != parent && null != parent.getChildren()) {
			for (final Tag t : parent.getChildren()) {
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
