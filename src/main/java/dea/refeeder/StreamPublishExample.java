package dea.refeeder;

import java.util.Collections;
import java.util.List;

import com.restfb.DefaultLegacyFacebookClient;
import com.restfb.Facebook;
import com.restfb.LegacyFacebookClient;
import com.restfb.Parameter;

public class StreamPublishExample {
	// You need to provide your access token here.
	// Instructions are available on http://restfb.com.
	private static final String MY_ACCESS_TOKEN = "AAACPptTOVfEBAIwMADFMYiZAXwXeU8TWfIZAVB7YwAFPzeZCHja88eZCSi9HJNYPNqq4MHicJqHt0kKTbl1M6tJGVXt9csdLZA9XwCNdL2QZDZD";

	public static void main(String[] args) {

		LegacyFacebookClient facebookClient = new DefaultLegacyFacebookClient(
				MY_ACCESS_TOKEN);

		ActionLink category = new ActionLink();
		category.href = "http://dea42.com";
		category.text = "news";

		Properties properties = new Properties();
		properties.category = category;
		properties.ratings = "5 stars";

		Medium medium = new Medium();
		medium.href = "http://dea42.com/img/swiss_thb.jpg";
		medium.src = "http://dea42.com/img/swiss_thb.jpg";
		medium.type = "image";

		Attachment attachment = new Attachment();
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
		String postId = facebookClient.execute("stream.publish", String.class,
				Parameter.with("attachment", attachment));

		System.out.println("Post ID is " + postId);
	}

	static class ActionLink {
		@Facebook
		String text;

		@Facebook
		String href;
	}

	static class Medium {
		@Facebook
		String type;

		@Facebook
		String src;

		@Facebook
		String href;
	}

	static class Properties {
		@Facebook
		ActionLink category;

		@Facebook
		String ratings;
	}

	static class Attachment {
		@Facebook
		String name;

		@Facebook
		String href;

		@Facebook
		String caption;

		@Facebook
		String description;

		@Facebook
		Properties properties;

		@Facebook
		List<Medium> media;
	}
}
