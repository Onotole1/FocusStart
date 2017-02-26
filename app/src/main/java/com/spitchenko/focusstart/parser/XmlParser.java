package com.spitchenko.focusstart.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.util.Log;

import com.spitchenko.focusstart.model.NewsModule;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Date: 25.02.17
 * Time: 20:36
 *
 * @author anatoliy
 */
abstract class XmlParser {
	DocumentBuilder dBuilder;

	XmlParser() {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	Node getNodeByTag(final NodeList rootList, final String tagName) {
		Node result = null;
		for (int i = 0; i < rootList.getLength(); i++) {
			if (rootList.item(i).getNodeName().equals(tagName)) {
				result = rootList.item(i);
			}
		}
		return result;
	}

	String getStringFromNode(final Node node, final String tagName) {
		String result = null;
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			if (node.getChildNodes().item(i).getNodeName().equals(tagName)) {
				Log.d("node", node.getChildNodes().item(i).getTextContent());
				result = node.getChildNodes().item(i).getTextContent();
			}
		}
		return result;
	}

	InputStream getInputStream(URL url) {
		try {
			return url.openConnection().getInputStream();
		} catch (IOException e) {
			return null;
		}
	}

	public abstract NewsModule parseXml(URL url);
}
