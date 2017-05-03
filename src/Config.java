import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class Config {
	
	static final String CONTROL_CHANNEL = "CONTROL_CHANNEL";
	static final String VOICE_CHANNEL = "VOICE_CHANNEL";
	static final String BOT_TOKEN = "BOT_TOKEN";
	static final String ADMIN_IDS = "ADMIN_IDS";
	static final String COMMAND_PREFIX = "COMMAND_PREFIX";
	static final String DISPLAY_SONG_AS_GAME = "DISPLAY_SONG_AS_GAME";
	static final String CHECK_FOR_UPDATES = "CHECK_FOR_UPDATES";
	
	private static Properties properties = new Properties();
	
	static boolean load() {
		try {
			if(Values.TESTING) {
				properties.load(new FileReader(new File("testingConfig.txt")));
			} else {
				properties.load(new FileReader(new File("config.txt")));
			}
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
		return value;
	}
	
	// For starting the bot with the bot token as argument
	static void overrideToken(String token) {
		properties.setProperty(BOT_TOKEN, token);
	}

}
