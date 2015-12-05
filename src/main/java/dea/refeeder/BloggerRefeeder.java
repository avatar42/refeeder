package dea.refeeder;

import java.io.UnsupportedEncodingException;
import java.rmi.server.ServerNotActiveException;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BloggerRefeeder {

	public static final boolean isDisabled = false;

	private final Logger log = LoggerFactory.getLogger(getClass());
	/**
	 * Mime constant
	 */
	private static final String MIME_TEXT = "text/html";
	private boolean debug = false;

	public BloggerRefeeder() {
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean sendAdmin(String subject, String content) {
		ResourceBundle bundle = ResourceBundle.getBundle("common");
		String toAddr = bundle.getString("blogger.to.addr");
		String smtpHost = bundle.getString("mail.smtp.host");
		String toName = "Blogger";
		String fromAddr = bundle.getString("from.addr");
		String fromName = bundle.getString("from.name");

		return sendAdmin(subject, content, toAddr, smtpHost, toName, fromAddr,
				fromName);
	}

	public boolean sendAdmin(String subject, String content, String toAddr,
			String smtpHost, String toName, String fromAddr, String fromName) {

		boolean rtn = false;

		if (isDisabled) {
			log.error("sendAdmin(" + subject + "," + content + " )");
		} else {
			try {

				Properties props = new Properties();
				props.put("mail.smtp.host", smtpHost);
				Session session = Session.getDefaultInstance(props, null);
				Message msg = new MimeMessage(session);

				InternetAddress iaAddr = new InternetAddress(toAddr, toName);
				InternetAddress[] iaToAddr = new InternetAddress[] { iaAddr };

				msg.setRecipients(Message.RecipientType.TO, iaToAddr);

				InternetAddress iaFromAddr = new InternetAddress(fromAddr,
						fromName);

				msg.setFrom(iaFromAddr);

				msg.setSubject(subject);
				msg.setSentDate(new Date());

				MimeMultipart mpRoot = new MimeMultipart("mixed");

				// Add html/body/text
				MimeBodyPart mbp2 = new MimeBodyPart();

				mbp2.setContent(content, MIME_TEXT);
				mpRoot.addBodyPart(mbp2);

				msg.setContent(mpRoot);
				msg.saveChanges();

				int tries = 1;
				while (tries < 4) {
					try {
						log.error("Send try=" + tries);
						if (props.get("mail.smtp.user") != null) {
							Transport transport = session.getTransport("smtp");
							transport.connect(
									(String) props.get("mail.smtp.host"),
									(String) props.get("mail.smtp.user"),
									(String) props.get("mail.smtp.pass"));
							if (transport.isConnected()) {
								transport.sendMessage(msg,
										msg.getAllRecipients());
								transport.close();
							} else {
								throw new ServerNotActiveException(
										"Failed to connect the smtp server.");
							}
						} else {
							Transport.send(msg);
						}
						rtn = true;
						break;
					} catch (MessagingException io) {
						log.error("Error sending:" + io.getMessage());
						if (tries == 3) {
							log.error("Tries exceeded, giving up");
							throw io;
						}
					}
					tries++;

				}
			} catch (UnsupportedEncodingException e) {
				log.error("Exception caught" + e);

			} catch (MessagingException e) {
				log.error("Exception caught" + e);

			} catch (ServerNotActiveException e) {
				log.error("Exception caught" + e);

			}
		}

		return rtn;
	}

	public void post(Feed feed, FeedMessage msg) {

		String descrption = msg.getDescription();
		String source = feed.getLink();
		if (source == null || source.trim().isEmpty())
			source = msg.getLink();

		StringBuilder content = new StringBuilder();

		content.append("posted this on ").append(feed.getTitle())
				.append("<br>\n");
		if (msg.getTitle() != null && !msg.getTitle().equals(descrption)) {
			content.append(descrption).append("<br>\n");
		}
		content.append("Source: <a href=\"").append(source).append("\">")
				.append(feed.getTitle()).append("</a><br>\n");
		content.append("Published: ").append(msg.getPubDateStr())
				.append("<br>\n");
		content.append("Read Full Story at ").append(msg.getLink())
				.append("<br>\n");

		content.append("Posted at ").append(new Date().toString())
				.append(" via Dea42 feeds\n");
		String postId = null;
		if (debug) {
			postId = "debug";
			System.out.println("message:" + msg.getTitle());
			System.out.println("attachment:" + content.toString());
		} else {
			if (sendAdmin(msg.getTitle().replaceAll("\n", " "),
					content.toString())) {
				postId = "sucess";
			}
		}

		if (postId != null) {
			feed.setPubDate(msg.getPubDate());
		}

		log.info("Post ID is " + postId);

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

		BloggerRefeeder fb = new BloggerRefeeder();
		fb.post(feed, msg);
	}

}
