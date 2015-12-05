/**
 *
 */
package dea.refeeder;

import com.restfb.DefaultLegacyFacebookClient;
import com.restfb.Facebook;
import com.restfb.LegacyFacebookClient;
import com.restfb.Parameter;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dea
 */
public class FaceBookRefeeder {

	private final Logger log = LoggerFactory.getLogger(getClass());

	// app page at https://developers.facebook.com/apps/157946697635313
	private static String MY_ACCESS_TOKEN = "PutInCommon.properties";

	private boolean debug = false;

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public FaceBookRefeeder() {
		ResourceBundle bundle = ResourceBundle.getBundle("common");
		MY_ACCESS_TOKEN = bundle.getString("fb.token");
	}

	/**
	 * unit test INFO: Executing a POST to
	 * https://api.facebook.com/restserver.php with parameters (sent in request
	 * body): access_token=
	 * AAACPptTOVfEBAFZCBeWzN8LaZAStDZCs8Jq5rWA3kwWADRcKfxBAmIkSdiVzJ6kJjf3XvvbvM2eW6QMVM2MZAPdHrOiRnzcZD
	 * & attachment={"description":null,"name":null,"caption":
	 * "{*actor*} posted this on Twitter / deabigt"
	 * ,"properties":{"Source":{"text":"Twitter / deabigt","href":""},
	 * "Published"
	 * :"Thu Nov 03 22:05:22 CDT 2011","Read":{"text":"Full Story","href"
	 * :"http://twitter.com/deabigt/statuses/132292323966070784"
	 * }},"media":null,"href":null} &format=json&message=deabigt: Already 37.9,
	 * 22 degrees cooler than last night at this time and falling faster. Best
	 * get those plants in.&method=stream.publish
	 * 
	 * INFO: Facebook responded with HTTP status code 200 and response body:
	 * {"error_code":100,"error_msg":
	 * "property values must be strings or objects with a 'text' and a 'href' attribute"
	 * , "request_args":[{"key":"access_token","value":
	 * "AAACPptTOVfEBAFZCBeWzN8LaZAStDZCs8Jq5rWA3kwWADRcKfxBAmIkSdiVzJ6kJjf3XvvbvM2eW6QMVM2MZAPdHrOiRnzcZD"
	 * }, {"key":"attachment","value":
	 * "{\"description\":null,\"name\":null,\"caption\":\"{*actor*} posted this
	 * on Twitter \/ deabigt\", \"properties\":{\"Source\":{\"text\":\"Twitter
	 * \/ deabigt\",\"href\":\"\"},\"Published\":\"Thu Nov 03 22:05:22 CDT
	 * 2011\", \"Read\":{\"text\":\"Full Story\
	 * ",\"href\":\"http:\/\/twitter.com\/deabigt\/statuses\/132292323966070784\"}},\"media\":null,\"
	 * h r e f \ " : n u l l }
	 * " } , {"key":"format","value":"json"},{"key":"message","value":
	 * "deabigt: Already 37.9, 22 degrees cooler than last night at this time and falling faster. Best get those plants in."
	 * }, {"key":"method","value":"stream.publish"}]}
	 */
	public void testPost() {

		LegacyFacebookClient facebookClient = new DefaultLegacyFacebookClient(
				MY_ACCESS_TOKEN);

		ActionLink category = new ActionLink();
		category.href = "http://dea42.com";
		category.text = "news";

		Properties properties = new Properties();
		properties.Source = category;
		properties.Published = new Date().toString();

		Medium medium = new Medium();
		medium.href = "http://dea42.com/img/swiss_thb.jpg";
		medium.src = "http://dea42.com/img/swiss_thb.jpg";
		medium.type = "image";

		AttachmentMedia attachment = new AttachmentMedia();
		attachment.name = "Post test";
		attachment.href = "http://dea42.com";
		attachment.caption = "{*actor*} posted this test";
		attachment.description = "test post";
		attachment.properties = properties;
		attachment.media = Collections.singletonList(medium);

		// Send the request to Facebook.
		// We specify the session key to use to make the call, the fact that
		// we're
		// expecting a String response, and the attachment (defined above).
		String postId;
		if (debug) {
			postId = "debug";
		} else {
			postId = facebookClient.execute("stream.publish", String.class,
					Parameter.with("attachment", attachment));
		}

		System.out.println("Post ID is " + postId);
	}

	public void post(Feed feed, FeedMessage msg) {
		LegacyFacebookClient facebookClient = new DefaultLegacyFacebookClient(
				MY_ACCESS_TOKEN);

		// Facebook no longer allows links to third party sites so only display
		// links
		// ActionLink source = new ActionLink();
		// source.href = feed.getLink();
		// if (source.href == null || source.href.trim().isEmpty())
		// source.href = msg.getLink();
		// source.text = feed.getTitle();

		// ActionLink read = new ActionLink();
		// read.href = msg.getLink();
		// read.text = "Full Story";

		Properties properties = new Properties();
		// properties.Source = source;
		properties.Published = msg.getPubDateStr();
		// properties.Read = read;

		String descrption = msg.getDescription();
		Attachment attachment;
		int start = descrption.indexOf("<img src=");
		if (start > -1) {
			start += 10;
			int end = descrption.indexOf('"', start);
			if (end > 1) {
				String imgHref = descrption.substring(start, end);
				AttachmentMedia mattachment = new AttachmentMedia();
				Medium medium = new Medium();
				medium.href = imgHref;
				medium.src = imgHref;
				medium.type = "image";
				mattachment.media = Collections.singletonList(medium);
				attachment = mattachment;
			} else {
				attachment = new Attachment();
			}
		} else {
			attachment = new Attachment();
		}
		// attachment.name = "Post test";
		// attachment.href = msg.getLink();
		attachment.caption = "{*actor*} tweeted " + msg.getLink() + " ";
		// if (msg.getTitle() != null && !msg.getTitle().equals(descrption)) {
		// attachment.description = msg.getDescription() + " from " +
		// msg.getLink();
		// }
		attachment.description = msg.getTitle();
		attachment.properties = properties;
		// Send the request to Facebook.
		// We specify the session key to use to make the call, the fact that
		// we're
		// expecting a String response, and the attachment (defined above).
		String postId;
		if (debug) {
			postId = "debug";
			System.out.println("message:" + msg.getTitle());
			System.out.println("attachment:" + attachment);
		} else {
			postId = facebookClient.execute("stream.publish", String.class,
					Parameter.with("message", msg.getTitle()),
					Parameter.with("attachment", attachment));
		}

		if (postId != null) {
			feed.setPubDate(msg.getPubDate());
		}

		log.info("Post ID is " + postId);

	}

	/*
	 * Feed{title='Twitter / deabigt', link='', description='Twitter updates
	 * from David Abigt / deabigt.', language='en-us', copyright='',
	 * pubDate=1320345948000, entries= FeedMessage{title='deabigt: Already 37.9,
	 * 22 degrees cooler than last night at this time and falling faster. Best
	 * get those plants in.', channel='null', description='deabigt: Already
	 * 37.9, 22 degrees cooler than last night at this time and falling faster.
	 * Best get those plants in.',
	 * link='http://twitter.com/deabigt/statuses/132292323966070784',
	 * author='null',
	 * guid='http://twitter.com/deabigt/statuses/132292323966070784',
	 * pubDate=1320375922000}
	 */
	/*
	 * FeedMessage{title='001- Painkiller Jane', channel='', description='<a
	 * href="http://www.netflix.com/Movie/Painkiller-Jane/70179981"><img
	 * src="http://cdn-0.nflximg.com/us/boxshots/small/70179981.jpg"
	 * /></a><br>With a mission that sounds impossible -- neutralizing mutants
	 * with superhuman intellects -- DEA agent Jane Vasco is lucky to have some
	 * superhuman qualities of her own (namely, the ability to recover from any
	 * injury) in this sci-fi drama.',
	 * link='http://www.netflix.com/Movie/Painkiller-Jane/70179981', author='',
	 * guid='http://www.netflix.com/Movie/Painkiller-Jane/70179981', pubDate=0}
	 */
	public static void main(String[] args) {
		Feed feed = new Feed("Test feed", "",
				"Test Netflix update from refeeder.", "en-us", "",
				1320345948000L);
		FeedMessage msg = new FeedMessage();
		msg.setTitle("deabigt: test post.");
		// msg.setChannel("null");
		msg.setDescription("<a href=\"http://www.netflix.com/Movie/Painkiller-Jane/70179981\"><img src=\"http://cdn-0.nflximg.com/us/boxshots/small/70179981.jpg\"/></a><br>With a mission that sounds impossible -- neutralizing mutants with superhuman intellects -- DEA agent Jane Vasco is lucky to have some superhuman qualities of her own (namely, the ability to recover from any injury) in this sci-fi drama.");
		msg.setLink("http://www.netflix.com/Movie/Painkiller-Jane/70179981");
		// msg.setAuthor("null");
		msg.setGuid("http://www.netflix.com/Movie/Painkiller-Jane/70179981");
		msg.setPubDate(0L);
		feed.add(msg);

		FaceBookRefeeder fb = new FaceBookRefeeder();
		fb.post(feed, msg);
	}

	/*
	 * INFO: Facebook responded with HTTP status code 200 and response body:
	 * {"error_code":100,"error_msg":
	 * "property values must be strings or objects with a 'text' and a 'href' attribute"
	 * ,"request_args":[ {"key":"access_token","value":
	 * "AAACPptTOVfEBAFZCBeWzN8LaZAStDZCs8Jq5rWA3kwWADRcKfxBAmIkSdiVzJ6kJjf3XvvbvM2eW6QMVM2MZAPdHrOiRnzcZD"
	 * },
	 * 
	 * {"key":"attachment","value":
	 * "{\"description\":\"\",\"name\":\"\",\"caption\":\"{*actor*} posted this
	 * on Twitter \/ deabigt\", \"properties\":{\"Source\":{\"text\":\"Twitter
	 * \/ deabigt\",\"href\":\"\"},\"Published\":\"Thu Nov 03 22:05:22 CDT
	 * 2011\", \"Read\":{\"text\":\"Full Story\
	 * ",\"href\":\"http:\/\/twitter.com\/deabigt\/statuses\/132292323966070784\"}},\"media\":null,\"href\":\"\"}"},
	 * 
	 * {"key":"format","value":"json"},
	 * {"key":"message","value":"deabigt: test post."},
	 * {"key":"method","value":"stream.publish"}]}
	 */
	class ActionLink {
		@Facebook
		String text = "Full story";

		@Facebook
		String href = "";

		@Override
		public String toString() {
			return "ActionLink [text=" + text + ", href=" + href + "]";
		}

	}

	class Medium {
		@Facebook
		String type = "";

		@Facebook
		String src = "";

		@Facebook
		String href = "";

		@Override
		public String toString() {
			return "Medium [type=" + type + ", src=" + src + ", href=" + href
					+ "]";
		}

	}

	class Properties {
		@Facebook
		ActionLink Read;

		@Facebook
		ActionLink Source;

		@Facebook
		String Published = "";

		@Override
		public String toString() {
			return "Properties [Read=" + Read + ", Source=" + Source
					+ ", Published=" + Published + "]";
		}

	}

	class Attachment {
		@Facebook
		String name = "";

		@Facebook
		String href = "";

		@Facebook
		String caption = "";

		@Facebook
		String description = "";

		@Facebook
		Properties properties;

		@Override
		public String toString() {
			return "Attachment [name=" + name + ", href=" + href + ", caption="
					+ caption + ", description=" + description
					+ ", properties=" + properties + "]";
		}

		// @Facebook
		// List<Medium> media;

	}

	class AttachmentMedia extends Attachment {
		@Facebook
		List<Medium> media;

		@Override
		public String toString() {
			return "AttachmentMedia [name=" + name + ", href=" + href
					+ ", caption=" + caption + ", description=" + description
					+ ", properties=" + properties + ", media=" + media + "]";
		}

	}
}
