package dea.refeeder;

import java.util.ResourceBundle;

public class WordpressRefeeder extends BloggerRefeeder {

	public WordpressRefeeder() {
	}

	public boolean sendAdmin(String subject, String content) {
		ResourceBundle bundle = ResourceBundle.getBundle("common");
		String toAddr = bundle.getString("wordpress.to.addr");
		String smtpHost = bundle.getString("mail.smtp.host");
		String toName = "Wordpress";
		String fromAddr = bundle.getString("from.addr");
		String fromName = bundle.getString("from.name");

		return sendAdmin(subject, content, toAddr, smtpHost, toName, fromAddr,
				fromName);
	}

	public static void main(String[] args) {
		Feed feed = new Feed("Twitter / deabigt", "",
				"Twitter updates from David Abigt / deabigt.", "en-us", "",
				1020375922000L);
		FeedMessage msg = new FeedMessage();
		msg.setTitle("test: Austin summary:rain chance/high today/low tonight 0%/70/49,\nSat 0%/77/63,Sun 0%/82/62,Mon 18%/83/64,\nTue 33%/79/54, Wed 0%/71/48,Thu 0%/71");
		// msg.setChannel("null");
		msg.setDescription("test: Austin summary:rain chance/high today/low tonight 0%/70/49,\nSat 0%/77/63,Sun 0%/82/62,Mon 18%/83/64,\nTue 33%/79/54, Wed 0%/71/48,Thu 0%/71");
		msg.setLink("http://twitter.com/deabigt/statuses/132292323966070784");
		// msg.setAuthor("null");
		msg.setGuid("http://twitter.com/deabigt/statuses/132292323966070784");
		msg.setPubDate(1020375922000L);
		feed.add(msg);

		WordpressRefeeder fb = new WordpressRefeeder();
		fb.post(feed, msg);
	}

}
