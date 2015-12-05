package dea.refeeder;

import java.util.Date;
import java.util.Iterator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
	public static final String NAME = Main.class.getName();
	private final Logger log = LoggerFactory.getLogger(getClass());
	boolean debug = false;
	boolean keepRunning = true;

	public void postToSites(Feed feed, FeedMessage msg, boolean postToFb) {
		if (postToFb) {
			FaceBookRefeeder fb = new FaceBookRefeeder();
			fb.setDebug(debug);
			fb.post(feed, msg);
		}

		BloggerRefeeder blog = new BloggerRefeeder();
		blog.setDebug(debug);
		blog.post(feed, msg);

		WordpressRefeeder wlog = new WordpressRefeeder();
		wlog.setDebug(debug);
		wlog.post(feed, msg);
	}

	public void postFeeds() {

		FeedList list = FeedHelper.getList();
		while (keepRunning) {
			Iterator<FeedListItem> it = list.iterator();
			while (it.hasNext()) {
				FeedListItem item = it.next();
				long lastPost = item.getLastRead();
				try {
					FeedParserInterface parser;
					if (item.getShortName().contains("twitter")) {
						parser = new TwitterFeedOauth(item);
					} else {
						parser = new RSSFeedParser(item);
					}
					// list.put(href, new
					// Date("Oct 31 07:33:42 CDT 2011").getTime());
					Feed feed = parser.readFeed();
					log.info(item.getShortName() + ":" + feed);
					if (feed != null) {
						log.info(item.getShortName() + ":"
								+ feed.getMessages().size());
						for (FeedMessage message : feed.getMessages()) {
							if (message.getPubDate() > 0) {
								log.info("lastPost="
										+ new Date(lastPost).toString());
								log.info("pubdate=" + message.getPubDateStr());
								if (lastPost == 0
										|| lastPost < message.getPubDate()) {
									lastPost = message.getPubDate();
									System.out.println(message);
									postToSites(feed, message,
											item.isPostToFb());
								}
							} else {
								log.info("title=" + message.getTitle());
								if (item.getLastTitle() == null
										|| !item.getLastTitle().equals(
												message.getTitle())) {
									item.setLastTitle(message.getTitle());
									System.out.println(message);
									postToSites(feed, message,
											item.isPostToFb());

								}
							}
						}
						item.setLastRead(feed.getPubDate());
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			FeedHelper.saveData(list);

			try {
				Thread.sleep(1000 * 60 * 30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isKeepRunning() {
		return keepRunning;
	}

	public void setKeepRunning(boolean keepRunning) {
		this.keepRunning = keepRunning;
	}

	public static void main(String[] args) {
		Main m = new Main();
		m.postFeeds();
	}

}
