package dea.refeeder;

import java.io.Serializable;

public class FeedListItem implements Serializable, Comparable<FeedListItem> {
	/**
     *
     */
	private static final long serialVersionUID = 1L;

	private String href;
	private long lastRead;
	// max titles to read
	private int maxToRead = Integer.MAX_VALUE;
	private String titleFilter = null;
	// if feed does not publish pub date so read top item and see if title is
	// same as last time to check for new.
	private String lastTitle = null;
	private String shortName = null;
	private boolean postToFb = true;

	public FeedListItem() {
	}

	public FeedListItem(String href, long lastRead, String shortName) {
		super();
		this.href = href;
		this.lastRead = lastRead;
		this.shortName = shortName;
	}

	public FeedListItem(String href, long lastRead, String shortName,
			int maxToRead, String titleFilter) {
		super();
		this.href = href;
		this.lastRead = lastRead;
		this.shortName = shortName;
		this.maxToRead = maxToRead;
		this.titleFilter = titleFilter;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public long getLastRead() {
		return lastRead;
	}

	public void setLastRead(long lastRead) {
		this.lastRead = lastRead;
	}

	public int getMaxToRead() {
		return maxToRead;
	}

	public void setMaxToRead(int maxToRead) {
		this.maxToRead = maxToRead;
	}

	public String getTitleFilter() {
		return titleFilter;
	}

	public void setTitleFilter(String titleFilter) {
		this.titleFilter = titleFilter;
	}

	public String getLastTitle() {
		return lastTitle;
	}

	public void setLastTitle(String lastTitle) {
		this.lastTitle = lastTitle;
	}

	public boolean isPostToFb() {
		return postToFb;
	}

	public void setPostToFb(boolean postToFb) {
		this.postToFb = postToFb;
	}

	public int compareTo(FeedListItem o) {
		if (o instanceof FeedListItem)
			return href.compareTo(((FeedListItem) o).getHref());

		return 0;
	}

	@Override
	public String toString() {
		return "FeedListItem [href=" + href + ", lastRead=" + lastRead
				+ ", maxToRead=" + maxToRead + ", titleFilter=" + titleFilter
				+ ", lastTitle=" + lastTitle + ", shortName=" + shortName
				+ ", postToFb=" + postToFb + "]";
	}
}
