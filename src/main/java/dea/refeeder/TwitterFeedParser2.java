package dea.refeeder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwitterFeedParser2 implements FeedParserInterface {
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

	public TwitterFeedParser2(FeedListItem feedInfo) {
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

	public static final String TWITTER_BASE_URL = "https://twitter.com";
	public static final String SECTION_KEY = "<div class=\"Grid\" data-component-term=\"tweet\" role=\"presentation\">";
	public static final String HREF_KEY = "<a class=\"ProfileTweet-timestamp u-textMute js-permalink js-nav js-tooltip\"";
	public static final String HREF_KEY2 = " href=\"";
	public static final String PUB_KEY = "data-time=";
	public static final String TWEET_START_KEY = "<p class=\"ProfileTweet-text js-tweet-text u-dir\"";
	public static final String TWEET_START_KEY2 = "dir=\"ltr\">";
	public static final String TWEET_END_KEY = "</p>";
	public static final String USER_START_KEY = "data-screen-name=\"";
	public static final String USER_END_KEY = "\"";
	public static final String RETWEET_KEY = "Retweeted by";

	/*
	 * The bit we care about <div class="stream-item-header"> <a class=
	 * "account-group js-account-group js-action-profile js-user-profile-link js-nav"
	 * href="/deabigt" data-user-id="16418410"> <img
	 * class="avatar js-action-profile-avatar"
	 * src="https://si0.twimg.com/profile_images/60568587/L_M5260008_normal.jpg"
	 * alt="David Abigt"> <strong
	 * class="fullname js-action-profile-name show-popup-with-id">David
	 * Abigt</strong> <span>&rlm;</span><span
	 * class="username js-action-profile-name"><s>@</s><b>deabigt</b></span>
	 * 
	 * </a> <small class="time"> <a href="/deabigt/status/348619170927235073"
	 * class="tweet-timestamp js-permalink js-nav"
	 * title="6:51 PM - 22 Jun 13"><span
	 * class="_timestamp js-short-timestamp js-relative-timestamp"
	 * data-time="1371952262" data-long-form="true">1h</span></a> </small>
	 * 
	 * </div>
	 * 
	 * 
	 * 
	 * 
	 * <p class="js-tweet-text tweet-text"> Was encouraged making it through
	 * last weekend with just 1 crash watching <a href="/HuluPlus"
	 * class="twitter-atreply pretty-link" dir="ltr"><s>@</s><b>HuluPlus</b></a>
	 * but it seems to back to regular drop outs this weekend. </p>
	 */
	private FeedMessage parseTweet(String xml) {
		FeedMessage message = new FeedMessage();
		boolean retweet = false;

		// String channel = "";
		int start = xml.indexOf(USER_START_KEY); // , end + 1);
		if (start > -1) {
			start += USER_START_KEY.length();
			int end = xml.indexOf(USER_END_KEY, start + 1);
			message.setAuthor(xml.substring(start, end));
		}
		start = xml.indexOf(HREF_KEY);
		if (start > -1) {
			start = xml.indexOf(HREF_KEY2, start + HREF_KEY.length());
			if (start > -1) {
				start = start + HREF_KEY2.length();
				int end = xml.indexOf('\"', start + 1);
				message.setLink(TWITTER_BASE_URL + xml.substring(start, end));
				message.setGuid(xml.substring(start, end));
				start = xml.indexOf(PUB_KEY, end + 1) + PUB_KEY.length() + 1;
				if (start > -1) {
					end = xml.indexOf('\"', start + 1);
					String lstr = xml.substring(start, end) + "000";
					message.setPubDate(Long.parseLong(lstr));
				}
			}
		}
		if (xml.indexOf(RETWEET_KEY) > -1) {
			retweet = true;
		}
		start = xml.indexOf(TWEET_START_KEY);
		if (start > -1) {
			start = xml.indexOf(TWEET_START_KEY2,
					start + TWEET_START_KEY.length());
			if (start > -1) {
				start = start + TWEET_START_KEY2.length();
				int end = xml.indexOf(TWEET_END_KEY, start + 1);
				StringBuilder sb = new StringBuilder();
				if (retweet) {
					sb.append("Retweeted:");
				}
				sb.append(message.getAuthor()).append(':');
				sb.append(xml.substring(start, end).replace("’", "\'")
						.replace("�?", "\"").replace("…", "...")
						.replace("–", "--").replace("“", "\""));
				message.setDescription(sb.toString());
				if (message.getDescription().indexOf("�") > -1) {
					log.warn("Special Char found in:"
							+ message.getDescription());
				}
				message.setTitle(stripTags(message.getDescription()));
			}
		}

		return message;
	}

	private String stripTags(String s) {
		int start = 0;
		int end = s.indexOf('<');
		if (end > -1) {
			StringBuilder sb = new StringBuilder();
			while (end > -1) {
				sb.append(s.substring(start, end));
				start = s.indexOf('>', end + 1) + 1;
				end = s.indexOf('<', start);
			}
			sb.append(s.substring(start));
			return StringEscapeUtils.unescapeHtml(sb.toString());
		}

		return s;
	}

	public Feed parseFeed() {
		Feed feed = new Feed(item.getShortName(), item.getHref(),
				"description", "language", "copyright",
				System.currentTimeMillis());

		int start = response.indexOf(SECTION_KEY);
		int end = -1;
		if (start > -1) {
			end = response.indexOf(SECTION_KEY, start + 1);
		}

		int found = 0;
		while (end > -1) {
			FeedMessage message = parseTweet(response.substring(start, end));
			if (message.getPubDate() > item.getLastRead()) {
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
			}
			if (found >= item.getMaxToRead()) {
				break;
			}
			start = end;
			end = response.indexOf(SECTION_KEY, start + 1);
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
			log.trace(response);

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
		// FeedListItem(String href, long lastRead, String shortName, int
		// maxToRead, String titleFilter) https://twitter.com/avatar_42 deabigt
		TwitterFeedParser2 rp = new TwitterFeedParser2(new FeedListItem(
				"https://twitter.com/deabigt", 1397352218000L,
				"twitter.deabigt", 100, null));
		rp.setDebug(true);
		try {
			Feed feed = rp.readFeed();
			// Feed feed = rp.loadFeed();
			for (FeedMessage message : feed.getMessages()) {
				System.out.println(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
