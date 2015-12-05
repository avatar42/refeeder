package dea.refeeder;

public interface FeedParserInterface {
	void setDebug(boolean debug);

	Feed parseFeed();

	Feed readFeed() throws Exception;

	/**
	 * reads feed from local file for testing
	 * 
	 * @return Feed object with current items in it
	 * @throws Exception
	 *             encountered
	 */
	Feed loadFeed() throws Exception;

}
