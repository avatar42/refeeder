package dea.refeeder;

import java.util.Date;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadTest {
	private static final Logger log = LoggerFactory.getLogger(ReadTest.class);

	public static void main(String[] args) {
		boolean debug = true;
		FaceBookRefeeder fb = new FaceBookRefeeder();
		fb.setDebug(debug);

		FeedList list = FeedHelper.getList();
		Iterator<FeedListItem> it = list.iterator();
		while (it.hasNext()) {
			FeedListItem item = it.next();
			long lastPost = item.getLastRead();
			try {
				RSSFeedParser parser = new RSSFeedParser(item);
				parser.setDebug(debug);
				// list.put(href, new
				// Date("Oct 31 07:33:42 CDT 2011").getTime());
				Feed feed = parser.readFeed();
				log.info(item.getShortName() + ":" + feed);
				if (feed != null) {
					for (FeedMessage message : feed.getMessages()) {
						if (message.getPubDate() > 0) {
							log.debug("lastPost="
									+ new Date(lastPost).toString());
							log.debug("pubdate=" + message.getPubDateStr());
							if (lastPost == 0
									|| lastPost < message.getPubDate()) {
								lastPost = message.getPubDate();
								System.out.println(message);
								fb.post(feed, message);
							}
						} else {
							if (item.getLastTitle() == null
									|| !item.getLastTitle().equals(
											message.getTitle())) {
								item.setLastTitle(message.getTitle());
								System.out.println(message);
								fb.post(feed, message);

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

		// FeedHelper.saveData(list);
	}
}