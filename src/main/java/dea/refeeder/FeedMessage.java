package dea.refeeder;

import java.util.Date;

/*
 * Represents one RSS message
 */
public class FeedMessage implements Comparable<FeedMessage> {

	String title = "";
	String channel = "";
	String description = "";
	String link = "";
	String author = "";
	String guid = "";
	long pubDate = 0L;

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public long getPubDate() {
		return pubDate;
	}

	public void setPubDate(long pubDate) {
		this.pubDate = pubDate;
	}

	public void setPubDate(String pubDate) {
		this.pubDate = new Date(pubDate).getTime();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getPubDateStr() {
		if (pubDate > 0)
			return new Date(pubDate).toString();
		else
			return new Date().toString(); //"unknown";
	}

	// @Override
	// public String toString() {
	// return "FeedMessage [pubDate=" + new Date(pubDate) + ", title=" + title
	// + ", description=" + description + ", link=" + link
	// + ", author=" + author + ", guid=" + guid + "]";
	// }

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("FeedMessage");
		sb.append("{title='").append(title).append('\'');
		sb.append(", channel='").append(channel).append('\'');
		sb.append(", description='").append(description).append('\'');
		sb.append(", link='").append(link).append('\'');
		sb.append(", author='").append(author).append('\'');
		sb.append(", guid='").append(guid).append('\'');
		sb.append(", pubDate=").append(pubDate).append("(").append(new Date(pubDate)).append(")");
		sb.append('}');
		return sb.toString();
	}

	public int compareTo(FeedMessage o) {
		if (o == null)
			return -1;

		if (getPubDate() > 0)
			return (int) (getPubDate() - o.getPubDate());
		else
			return getTitle().compareTo(o.getTitle());
	}

}
