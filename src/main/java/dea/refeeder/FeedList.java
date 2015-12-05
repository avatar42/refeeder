package dea.refeeder;

import java.util.Iterator;
import java.util.TreeSet;

public class FeedList extends TreeSet<FeedListItem> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FeedList() {
	}

	public void init() {
		add(new FeedListItem(
				"https://api.twitter.com/1/statuses/user_timeline.rss?screen_name=deabigt",
				1320250149000L, "twitter.deabigt"));
		add(new FeedListItem(
				"http://twitter.com/statuses/user_timeline/38239423.rss",
				1320250149000L, "twitter.avatar_42"));
		add(new FeedListItem(
				"http://rss.netflix.com/QueueEDRSS?id=P8948940922721742784481854194485164",
				0L, "netflix.instantQue", 1, "001-"));
//		add(new FeedListItem("http://youtube.com/rss/tag/deabigt.rss", 0L,
//				"youtube.deabigt"));
	}

	// public void add(String i, Long lastRead) {
	// put(i, lastRead);
	// }

	public Iterator<FeedListItem> iterator() {
		// TODO: remove
		// if file was corrupted reinit
		if (isEmpty())
			init();

		return super.iterator();
		// return keySet().iterator();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("FeedList [");
		if (!isEmpty()) {
			Iterator<FeedListItem> it = super.iterator();
			while (it.hasNext()) {
				sb.append('\n').append(it.next());
			}
		}

		sb.append("]");

		return sb.toString();
	}

	// public Long getLastRead(String key) {
	//
	// return get(key);
	// }

}
