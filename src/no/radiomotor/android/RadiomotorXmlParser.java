package no.radiomotor.android;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RadiomotorXmlParser {

	private String ns = null;

	public List<Item> parse(InputStream stream) throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(stream, null);
			parser.nextTag();
			return readRss(parser);
		} finally {
			stream.close();
		}
	}

	private List<Item> readRss(XmlPullParser parser) throws IOException, XmlPullParserException {
		List<Item> entries = new ArrayList<Item>();

		parser.require(XmlPullParser.START_TAG, ns, "rss");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			parser.require(XmlPullParser.START_TAG, ns, "channel");
			while (parser.next() != XmlPullParser.END_TAG) {
				if (parser.getEventType() != XmlPullParser.START_TAG) {
					continue;
				}
				String name = parser.getName();
				if (name.equals("item")) {
					entries.add(readItem(parser));
				} else {
					skip(parser);
				}
			}
		}
		return entries;
	}

	public static class Item implements Serializable {
		public String title;
		public String link;
		public String content;

		public Item(String title, String link, String content) {
			this.title = title;
			this.link = link;
			this.content = content;
		}

		@Override
		public String toString() {
			return "Item{" +
					"title='" + title + '\'' +
					", link='" + link + '\'' +
					'}';
		}
	}

	public Item readItem(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "item");
		String title = null;
		String link = null;
		String content = null;
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("title")) {
				title = readTag(parser, "title");
			} else if (name.equals("link")) {
				link = readTag(parser, "link");
			} else if (name.equals("content:encoded")) {
				content = readContent(parser);
			} else {
				skip(parser);
			}
		}
		return new Item(title, link, content);
	}

	private String readTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, tag);
		String text = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, tag);
		return text;
	}

	private String readContent(XmlPullParser parser) throws IOException, XmlPullParserException {
		parser.require(XmlPullParser.START_TAG, ns, "content:encoded");
		String content = readText(parser);
		// TODO remove HTML-tags etc
		parser.require(XmlPullParser.END_TAG, ns, "content:encoded");
		return content;
	}

	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
				case XmlPullParser.END_TAG:
					depth--;
					break;
				case XmlPullParser.START_TAG:
					depth++;
					break;
			}
		}
	}
}