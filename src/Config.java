import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.commons.io.output.FileWriterWithEncoding;

public class Config {

	static final String CONTROL_CHANNEL = "CONTROL_CHANNEL";
	static final String VOICE_CHANNEL = "VOICE_CHANNEL";
	static final String BOT_TOKEN = "BOT_TOKEN";
	static final String ADMIN_IDS = "ADMIN_IDS";
	static final String COMMAND_PREFIX = "COMMAND_PREFIX";
	static final String DISPLAY_SONG_AS_GAME = "DISPLAY_SONG_AS_GAME";
	static final String UPDATE_CHECK_INTERVAL_HOURS = "UPDATE_CHECK_INTERVAL_HOURS";

	private static Properties properties = new Properties();


	//TODO: Default Config Enum: seperate value and key to check if key exists (for config updates)
	private static final String[] DEFAULT_CONFIG = {"BOT_TOKEN=#uncomment this (remove \"#\") and put bot token here. You can create your app / bot and get your token here: https://discordapp.com/developers/applications/me",
			"CONTROL_CHANNEL=bot",
			"COMMAND_PREFIX=/",
			"VOICE_CHANNEL=Music",
			"DISPLAY_SONG_AS_GAME=true",
			"UPDATE_CHECK_INTERVAL_HOURS=24 #set to 0 to disable",
	"ADMIN_IDS=#uncomment this and put admin IDs here splitted with \":\""};


	static boolean load(File configFile) {
		try {
			properties.load(new FileReader(configFile));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	static String get(String key) {
		String value = properties.getProperty(key);
		if(value == null) {
			Log.crash("Config value not found: " + key);
		}
		return value.split("#")[0].trim(); // ignore comments, trim
	}

	static boolean generate(File configFile) {
		//TODO: Test with ProGuard (should remove String arr from Values
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(configFile, Charset.forName("UTF-8")));
			for(String s : DEFAULT_CONFIG) {
				writer.write(s + System.lineSeparator());
			}
			writer.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	static void update(File configFile) {
		//TODO
		// Enum ... see DEFAULT_CONFIG
	}

}
