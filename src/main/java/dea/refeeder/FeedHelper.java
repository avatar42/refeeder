package dea.refeeder;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeedHelper {
	public static final String APP_DATA_FILENAME = "feedData.xml";
	public static final String NAME = FeedHelper.class.getName();
	private static final Logger log = LoggerFactory.getLogger(FeedHelper.class);

	public static FeedList getList() {
		FeedList data = null;
		FileInputStream in = null;
		XMLDecoder d;
		try {
			File file = new File(APP_DATA_FILENAME);
			if (file.exists()) {
				in = new FileInputStream(file);
				d = new XMLDecoder(in);
				data = (FeedList) d.readObject();
				d.close();
				log.error(file.getAbsolutePath() + " Loaded");
			} else {
				log.error(file.getAbsolutePath() + " Not found");
			}

		} catch (FileNotFoundException e) {
			log.error(NAME, "getList()", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.error(NAME, "getList().close()", e);
				}
			}
		}

		if (data == null) {
			data = new FeedList();
			data.init();
		}

		log.warn("Using: " + data);

		return data;

	}

	public static void saveData(FeedList data) {
		log.info("Saving:" + data);
		FileOutputStream out = null;
		XMLEncoder e = null;
		try {
			File file = new File(APP_DATA_FILENAME);
			out = new FileOutputStream(file);
			e = new XMLEncoder(new BufferedOutputStream(out));
			e.writeObject(data);
			log.warn("List saved to " + file.getAbsolutePath());
		} catch (IOException e1) {
			log.error(NAME, "saveData(FeedList file)", e1);
			e1.printStackTrace();
		} finally {
			if (e != null) {
				e.close();
			}
		}

	}

}
