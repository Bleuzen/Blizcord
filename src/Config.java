import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.io.output.FileWriterWithEncoding;

public class Config {

	static final String CONTROL_CHANNEL = "CONTROL_CHANNEL";
	static final String VOICE_CHANNEL = "VOICE_CHANNEL";
	static final String BOT_TOKEN = "BOT_TOKEN";
	static final String COMMAND_PREFIX = "COMMAND_PREFIX";
	static final String DISPLAY_SONG_AS_GAME = "DISPLAY_SONG_AS_GAME";
	static final String UPDATE_CHECK_INTERVAL_HOURS = "UPDATE_CHECK_INTERVAL_HOURS";
	static final String ADMINS_ROLE = "ADMINS_ROLE";

	private static Properties properties;

	static boolean init(File configFile) {
		Properties defaults = new Properties();
		defaults.setProperty(ADMINS_ROLE, "Admins #comment out or delete everything behind \"=\" to disable");
		defaults.setProperty(BOT_TOKEN, "#uncomment this (remove \"#\") and put bot token here. You can create your app / bot and get your token here: https://discordapp.com/developers/applications/me");
		defaults.setProperty(CONTROL_CHANNEL, "bot");
		defaults.setProperty(COMMAND_PREFIX, "!");
		defaults.setProperty(VOICE_CHANNEL, "Music");
		defaults.setProperty(DISPLAY_SONG_AS_GAME, "true");
		defaults.setProperty(UPDATE_CHECK_INTERVAL_HOURS, "24 #set to 0 to disable");

		Properties loaded = new Properties();
		try {
			loaded.load(new FileReader(configFile));
		} catch (Exception e) {
			// Failed to load config. Maybe it doesn't exist. Try to generate it later by adding all missing values.
		}

		Enumeration<Object> keys = defaults.keys();
		ArrayList<String> toAdd = null;
		while(keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if(!loaded.containsKey(key)) {
				if(toAdd == null) {
					toAdd = new ArrayList<>();
				}
				toAdd.add(key + "=" + defaults.getProperty(key));
			}
		}

		if(toAdd == null) {
			properties = loaded;
			return true;
		} else {
			if(generate(configFile, toAdd)) {
				a.errExit("Config file got generated or updated. Please edit it and restart me.");
			} else {
				a.errExit("Failed to generate config. (Do you have write access here?)");
			}
			return false; // will never get called, but Eclipse wants it
		}

	}

	static String get(String key) {
		String value = properties.getProperty(key);
		if(value == null) {
			a.errExit("Config value not found: " + key);
		}
		return value.split("#")[0].trim(); // ignore comments, trim
	}

	private static boolean generate(File configFile, ArrayList<String> toAdd) {
		Collections.sort(toAdd);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(configFile, Charset.forName("UTF-8"), true));
			if(needNewLine(configFile)) {
				writer.newLine();
			}
			for(String s : toAdd) {
				writer.write(s);
				writer.newLine();
			}
			writer.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private static boolean needNewLine(File file) {
		try {
			RandomAccessFile fileHandler = new RandomAccessFile(file, "r");
			long fileLength = fileHandler.length() - 1;
			if (fileLength < 0) {
				fileHandler.close();
				return false;
			}
			fileHandler.seek(fileLength);
			byte readByte = fileHandler.readByte();
			fileHandler.close();

			if (readByte == 0xA || readByte == 0xD) {
				return false;
			} else {
				return true;
			}
		} catch(Exception e) {}
		return false;
	}

}