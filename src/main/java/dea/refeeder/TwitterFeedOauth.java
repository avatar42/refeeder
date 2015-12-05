package dea.refeeder;

import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterFeedOauth implements FeedParserInterface {
	static final int MAX_TITLE_LEN = 80;
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

	private final Logger log = LoggerFactory.getLogger(getClass());

	private String consumerKey;
	private String consumerSecret;
	private String userName;
	private String accessToken;
	private String accessTokenSecret;
	private ResourceBundle bundle;

	private boolean debug = false;
	private FeedListItem item = null;

	public TwitterFeedOauth(FeedListItem feedInfo) {
		item = feedInfo;
		int idx = item.getShortName().indexOf('.');
		if (idx > -1) {
			bundle = ResourceBundle.getBundle("twitter4j");
			userName = item.getShortName().substring(idx + 1).trim();
			consumerKey = bundle.getString("oauth.consumerKey");
			consumerSecret = bundle.getString("oauth.consumerSecret");
			accessToken = bundle.getString(userName + ".oauth.accessToken");
			accessTokenSecret = bundle.getString(userName
					+ ".oauth.accessTokenSecret");
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
		Feed feed = new Feed(item.getShortName(), item.getHref(), DESCRIPTION,
				LANGUAGE, COPYRIGHT, System.currentTimeMillis());

		try {
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true)
					// DeaUpdates keys
					.setOAuthConsumerKey(consumerKey)
					.setOAuthConsumerSecret(consumerSecret)
					// account codes
					.setOAuthAccessToken(accessToken)
					.setOAuthAccessTokenSecret(accessTokenSecret)
					.setJSONStoreEnabled(true);
			TwitterFactory tf = new TwitterFactory(cb.build());
			// The factory instance is re-useable and thread safe.
			Twitter twitter = tf.getInstance();
			List<Status> statuses = twitter.getUserTimeline();
			System.out.println("Showing home timeline.");
			for (Status status : statuses) {
				if (item.getLastRead() < status.getCreatedAt().getTime()) {
					if (debug) {
						log.info(status.toString());
					}
					FeedMessage message = new FeedMessage();
					message.setPubDate(status.getCreatedAt().getTime());

					if (status.isRetweet()) {
						status = status.getRetweetedStatus();
					}
					message.setAuthor(status.getUser().getName());
					// message.setChannel(channel);
					message.setDescription("@"
							+ status.getUser().getScreenName()
							+ ":"
							+ status.getText().replace("’", "\'")
									.replace("�?", "\"").replace("…", "...")
									.replace("–", "--").replace("“", "\""));
					message.setGuid(status.getUser().getScreenName() + "/"
							+ status.getId());
					message.setLink(TWITTER_BASE_URL + "/"
							+ status.getUser().getScreenName() + "/status/"
							+ status.getId());
					int idx = message.getDescription().indexOf('\r');
					if (idx == -1)
						idx = message.getDescription().indexOf('\n');
					if (message.getDescription().length() < MAX_TITLE_LEN
							&& (idx == -1 || idx > MAX_TITLE_LEN)) {
						message.setTitle(stripTags(message.getDescription()));
					} else {
						if (idx == -1 || idx > MAX_TITLE_LEN)
							idx = MAX_TITLE_LEN;
						message.setTitle(stripTags(message.getDescription())
								.substring(0, idx - 4) + "....");
					}
					feed.add(message);
				}
			}
		} catch (TwitterException e) {
			log.error("Exception parsing Twitter feed " + item.getShortName(),
					e);
		}

		return feed;

	}

	public Feed readFeed() throws Exception {
		return parseFeed();
	}

	/**
	 * reads feed from local file for testing but in this case that is not
	 * possible since is calling API so same as readFeed()
	 * 
	 * @return Feed object with current items in it
	 * @throws Exception
	 *             encountered
	 */
	public Feed loadFeed() throws Exception {
		return parseFeed();

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
		TwitterFeedOauth rp = new TwitterFeedOauth(new FeedListItem(
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
