package com.spitchenko.focusstart.utils.parser;

import com.spitchenko.focusstart.utils.logger.LogCatHandler;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

/**
 * Date: 25.02.17
 * Time: 20:36
 *
 * @author anatoliy
 */
final class XmlParser {
    private final static int MAX_DEPTH = 5;
    private final static String XML_PULL_PARSER
            = "com.spitchenko.focusstart.utils.parser.XmlParser";
    private final static String DEPTH_EXCEPTION = XML_PULL_PARSER + ".depthException";
	private XmlPullParser xpp;
    private InputStream inputStream;

	XmlParser(@NonNull final String url) throws IOException, XmlPullParserException {
        final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        xpp = factory.newPullParser();
        inputStream = getInputStream(new URL(url));
        if (null != inputStream) {
            xpp.setInput(inputStream, null);
        } else {
            throw new IOException();
        }
	}

	Tag readTags() throws IOException, XmlPullParserException {
        Tag parent = null;
        Tag nextTag = null;
        int depth = 0;
            int eventType = xpp.getEventType();
            final StringBuilder stringBuilder = new StringBuilder();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    final int currentDepth = xpp.getDepth();
                    nextTag = new Tag(xpp.getName(), parent, currentDepth);
                    if (null == parent) {
                        parent = nextTag;
                    } else if (currentDepth > depth) {
                        parent.children.add(nextTag);
                        parent = nextTag;
                    } else if (currentDepth < depth) {
                        while (currentDepth - parent.getDepth() != 1) {
                            parent = parent.parent;
                        }
                        nextTag.setParent(parent);
                        parent.children.add(nextTag);
                        parent = nextTag;
                    } else if (currentDepth == depth) {
                        if (parent.getDepth() == nextTag.getDepth()) {
                            parent = parent.parent;
                            nextTag.setParent(parent);
                        }
                        parent.children.add(nextTag);
                        parent = nextTag;
                    }
                    for (int i = 0, size = xpp.getAttributeCount(); i < size; i++) {
                        nextTag.getAttributes().put(xpp.getAttributeName(i)
                                , xpp.getAttributeValue(i));
                    }
                    depth = xpp.getDepth();
                } else if ((eventType == XmlPullParser.TEXT || eventType == XmlPullParser.CDSECT)
                        && null != xpp.getText() && null != nextTag) {
                    if (null != xpp.getText()) {
                        stringBuilder.append(xpp.getText());
                    }
                } else if (eventType == XmlPullParser.ENTITY_REF && null != xpp.getText()
                        && null != nextTag) {
                    if (null != xpp.getText()) {
                        stringBuilder.append(xpp.getText());
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (null != nextTag && !stringBuilder.toString().trim().isEmpty()) {
                        nextTag.setText(stringBuilder.toString().trim());
                        stringBuilder.delete(0, stringBuilder.length());
                    }
                }
                eventType = xpp.nextToken();
                if (depth > MAX_DEPTH) {
                    throw new XmlPullParserException(DEPTH_EXCEPTION);
                }
            }

            parent = getRoot(parent);

        inputStream.close();
        return parent;
    }

	private InputStream getInputStream(@NonNull final URL url) {
		try {
			return url.openConnection().getInputStream();
		} catch (final IOException e) {
            LogCatHandler.publishInfoRecord(e.getMessage());
			return null;
		}
	}

	private Tag getRoot(@NonNull final Tag tag) {
		Tag workingTag = tag;
		while (null != workingTag.getParent()) {
            workingTag = workingTag.getParent();
        }
		return workingTag;
	}

	String getValueTag(@NonNull final String inputTag, @NonNull final String parentTag
            , @NonNull final Tag tag) {
		final Tag parent = getCurrentTagByParent(parentTag, tag);
		if (null != parent && null != parent.getChildren()) {
            final List<Tag> children = parent.getChildren();
            for (int i = 0, size = children.size(); i < size; i++) {
                final Tag tagChild = children.get(i);
                if (tagChild.getName().equals(inputTag)) {
                    return tagChild.getText();
                }
            }
		}
		return null;
	}

	Tag getCurrentTagByParent(@NonNull final String inputTag, @NonNull final Tag tag) {
        final ArrayList<Tag> tagsQueue = new ArrayList<>();
        if (inputTag.equals(tag.getName())) {
            return tag;
        } else {
            final List<Tag> children = tag.getChildren();
            for (int i = 0, size = children.size(); i < size; i++) {
                final Tag tagChild = children.get(i);
                if (tagChild.getName().equals(inputTag)) {
                    return tagChild;
                } else {
                    tagsQueue.add(tagChild);
                }
            }

            for (int i = 0, size = tagsQueue.size(); i < size; i++) {
                final Tag tagFromQueue = tagsQueue.get(i);
                if (null != tagFromQueue.getChildren()) {
                    final List<Tag> childrenOfTagFormQueue = tagFromQueue.getChildren();
                    for (int j = 0, size1 = childrenOfTagFormQueue.size(); j < size1; j++) {
                        final Tag childOfTagFromQueue = childrenOfTagFormQueue.get(j);
                        if (childOfTagFromQueue.getName().equals(inputTag)) {
                            return childOfTagFromQueue;
                        }
                    }
                }
            }
        }
        return null;
	}
}
