package dea.refeeder;

import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;

/*
 * Stores an RSS feed
 */
public class Feed {

	private String title;
	private String link;
	private String description;
	private String language;
	private String copyright;
	private long pubDate = 0;
	private transient TreeSet<FeedMessage> entries = new TreeSet<FeedMessage>();

	public Feed(String title, String link, String description, String language,
			String copyright, long pubDate) {
		this.title = title;
		this.link = link;
		this.description = description;
		this.language = language;
		this.copyright = copyright;
		this.pubDate = pubDate;
	}

	public TreeSet<FeedMessage> getMessages() {
		return entries;
	}

	public boolean add(FeedMessage e) {
		return entries.add(e);
	}

	public String getTitle() {
		return title;
	}

	public String getLink() {
		return link;
	}

	public String getDescription() {
		return description;
	}

	public String getLanguage() {
		return language;
	}

	public String getCopyright() {
		return copyright;
	}

	public long getPubDate() {
		return pubDate;
	}

	public void setPubDate(long pubDate) {
		this.pubDate = pubDate;
	}

	public String getPubDateStr() {
		if (pubDate > 0)
			return new Date(pubDate).toString();
		else
			return "unknown";
	}

	// @Override
	// public String toString() {
	// return "Feed [copyright=" + copyright + ", description=" + description
	// + ", language=" + language + ", link=" + link + ", pubDate="
	// + getPubDateStr() + ", title=" + title + "]";
	// }

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Feed");
		sb.append("{title='").append(title).append('\'');
		sb.append(", link='").append(link).append('\'');
		sb.append(", description='").append(description).append('\'');
		sb.append(", language='").append(language).append('\'');
		sb.append(", copyright='").append(copyright).append('\'');
		sb.append(", pubDate=").append(pubDate);
		sb.append(", entries=");
		if (entries != null && !entries.isEmpty()) {
			Iterator<FeedMessage> it = entries.iterator();
			while (it.hasNext()) {
				sb.append('\n').append(it.next());
			}
		}
		sb.append('}');
		return sb.toString();
	}
}