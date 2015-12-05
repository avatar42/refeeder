package dea.refeeder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RSSFeedParser implements FeedParserInterface {
	private final Logger log = LoggerFactory.getLogger(getClass());

	static final String TITLE = "title";
	static final String DESCRIPTION = "description";
	static final String CHANNEL = "channel";
	static final String LANGUAGE = "language";
	static final String COPYRIGHT = "copyright";
	static final String LINK = "link";
	static final String AUTHOR = "author";
	static final String ITEM = "item";
	static final String PUB_DATE = "pubDate";
	static final String GUID = "guid";

	private String response = null;

	private URL conn = null;

	private boolean debug = false;

	private FeedListItem item = null;

	public RSSFeedParser(FeedListItem feedInfo) {
		item = feedInfo;
		try {
			this.conn = new URL(item.getHref());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * get tags value
	 * 
	 * @param eventReader
	 *            XMLEventReader
	 * @param evtName
	 *            TODO
	 * @return tags value as String
	 * @throws XMLStreamException
	 *             if fails parsing the tags XML to get the value
	 */
	private String getVal(XMLEventReader eventReader, String evtName)
			throws XMLStreamException {
		StringBuilder rtn = new StringBuilder();
		XMLEvent event = eventReader.nextEvent(); // throws XMLStreamException

		while (!event.isEndElement()) {
			if (event.isCharacters()) {
				rtn.append(event.asCharacters().getData());
				log.trace(evtName + ":" + rtn);
			} else {
				log.error("Not expected string:"
						+ event.getLocation().getPublicId() + ":"
						+ event.getEventType() + ":" + event.toString());
			}

			event = eventReader.nextEvent(); // throws XMLStreamException
		}
		return rtn.toString();
	}

	public Feed parseFeed() {
		Feed feed = null;
		XMLEvent event = null;
		ByteArrayInputStream bais = null;
		long pubdate = item.getLastRead();
		try {
			FeedMessage message = new FeedMessage();

			boolean isFeedHeader = true;
			// Set header values initially to the empty string
			String title = "";
			String link = "";
			String description = "";
			String language = "";
			String copyright = "";

			// Setup a new eventReader

			// First create a new XMLInputFactory
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			inputFactory.setProperty(
					XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);

			bais = new ByteArrayInputStream(response.getBytes());

			XMLEventReader eventReader = inputFactory
					.createXMLEventReader(bais);
			int found = 0;
			// Read the XML document
			while (eventReader.hasNext()) {

				event = eventReader.nextEvent();
				// System.out.println(event.getLocation().getPublicId() + ":"
				// + event.getEventType() + ":" + event.toString());
				if (event.isStartElement()) {
					String evtName = event.asStartElement().getName()
							.getLocalPart();
					// System.out.println("StartElement:"
					// + event.asStartElement().getName().getLocalPart());
					if (evtName.equals(ITEM)) {
						if (isFeedHeader) {
							isFeedHeader = false;
							feed = new Feed(title, link, description, language,
									copyright, pubdate);
						}
						event = eventReader.nextEvent();
					} else if (evtName.equals(AUTHOR)) {
						message.setAuthor(getVal(eventReader, evtName));
					} else if (evtName.equals(TITLE)) {
						title = getVal(eventReader, evtName);
						message.setTitle(title);
						// } else if (evtName.equals(CHANNEL)) {
						// message.setChannel(getVal(eventReader));
					} else if (evtName.equals(DESCRIPTION)) {
						try {
							description = getVal(eventReader, evtName);
							message.setDescription(description);
						} catch (XMLStreamException e) {
							log.warn(e.getMessage());
						}
					} else if (evtName.equals(LINK)) {
						link = getVal(eventReader, evtName);
						message.setLink(link);
					} else if (evtName.equals(GUID)) {
						message.setGuid(getVal(eventReader, evtName));
					} else if (evtName.equals(LANGUAGE)) {
						language = getVal(eventReader, evtName);
					} else if (evtName.equals(PUB_DATE)) {
						message.setPubDate(getVal(eventReader, evtName));
					} else if (evtName.equals(COPYRIGHT)) {
						copyright = getVal(eventReader, evtName);
					}
				} else if (event.isEndElement()) {
					// System.out.println("EndElement:"
					// + event.asEndElement().getName().getLocalPart());
					if (event.asEndElement().getName().getLocalPart()
							.equals(ITEM)) {
						if (item.getTitleFilter() == null) {
							feed.add(message);
							found++;
						} else {
							if (message.getTitle() != null
									&& message.getTitle().contains(
											item.getTitleFilter())) {
								feed.add(message);
								found++;
							}
						}
						if (found >= item.getMaxToRead()) {
							break;
						}
						message = new FeedMessage();
						event = eventReader.nextEvent();
					}
				}
			}
		} catch (Exception e) {
			if (event != null)
				System.err.println(event.getLocation().getPublicId() + ":"
						+ event.getEventType() + ":" + event.toString());
			else
				System.err.println("Event is null");

			throw new RuntimeException(e);
		} finally {
			if (bais != null)
				try {
					bais.close();
				} catch (IOException e) {
					e.printStackTrace(); // do not care
				}
		}

		return feed;

	}

	public Feed readFeed() throws Exception {
		Feed feed = null;
		String feedName = item.getShortName();
		if (read() > 0) {
			try {
				feed = parseFeed();
			} catch (Exception e) {
				File f = new File(feedName + ".rss");
				if (!f.renameTo(new File(feedName + "."
						+ System.currentTimeMillis() + ".rss")))
					log.warn("Rename failed for " + feedName + ".rss to "
							+ feedName + "." + System.currentTimeMillis()
							+ ".rss");
				throw e;
			}
		}
		return feed;

	}

	/**
	 * reads feed from local file for testing
	 * 
	 * @return Feed object with current items in it
	 * @throws Exception
	 *             encountered
	 */
	public Feed loadFeed() throws Exception {
		Feed feed = null;
		if (load() > 0) {
			feed = parseFeed();
		}
		return feed;

	}

	private int load() throws Exception {

		int read = 0;
		BufferedReader br = null;
		try {
			StringBuilder sb = new StringBuilder(200);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					item.getShortName() + ".rss")));
			String s;
			while ((s = br.readLine()) != null) {
				read += s.length();
				sb.append(s);
				sb.append('\n');
			}

			response = sb.toString().trim();

		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return read;
	}

	/**
	 * Read the feed into memory so we can fix it up if need be.
	 * 
	 * @return int of bytes read
	 * @throws Exception
	 *             encountered
	 */
	private int read() throws Exception {

		int read = 0;
		BufferedReader br = null;
		try {
			StringBuilder sb = new StringBuilder(200);
			HttpURLConnection connection;
			if (conn.getProtocol().startsWith("https:")) {
				connection = (HttpsURLConnection) conn.openConnection();
			} else {
				connection = (HttpURLConnection) conn.openConnection();
			}
			br = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String s;
			while ((s = br.readLine()) != null) {
				read += s.length();
				sb.append(s);
				sb.append('\n');
			}

			response = sb.toString().trim();

		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// save it to disk for debug
		if (read > 0 && debug) {
			File f = new File(item.getShortName() + ".rss");
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(response.getBytes());
			fos.flush();
			fos.close();
			log.info("Saved feed to " + f.getAbsolutePath());
		}

		return read;
	}

	/**
	 * for testing only
	 * 
	 * @param args
	 *            ignored
	 */
	public static void main(String[] args) {
		RSSFeedParser rp = new RSSFeedParser(
				new FeedListItem(
						"http://rss.netflix.com/QueueEDRSS?id=P8948940922721742784481854194485164",
						0L, "netflix.instantQue", 2, "00"));
		rp.setDebug(true);
		try {
			// Feed feed = rp.readFeed();
			Feed feed = rp.loadFeed();
			System.out.println(feed);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
