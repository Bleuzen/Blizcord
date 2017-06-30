import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONObject;
import org.json.JSONTokener;

public class Config {

	static final String CONTROL_CHANNEL = "CONTROL_CHANNEL";
	static final String VOICE_CHANNEL = "VOICE_CHANNEL";
	static final String BOT_TOKEN = "BOT_TOKEN";
	static final String COMMAND_PREFIX = "COMMAND_PREFIX";
	static final String DISPLAY_SONG_AS_GAME = "DISPLAY_SONG_AS_GAME";
	static final String UPDATE_CHECK_INTERVAL_HOURS = "UPDATE_CHECK_INTERVAL_HOURS";
	static final String ADMINS_ROLE = "ADMINS_ROLE";
	static final String VOLUME = "VOLUME";

	private static File DEFAULT_CONFIG = null;

	private static JSONObject defaults;
	private static boolean initialized;

	private static File file;
	private static JSONObject json;

	static boolean init(File configFile) {
		return init(configFile, false);
	}

	static boolean init(File configFile, boolean fromConfigGUI) { // don't crash after generation if fromConfigGUI
		if(initialized) {
			return true;
		}

		file = configFile;

		defaults = new JSONObject();
		defaults.put(BOT_TOKEN, "#uncomment this (remove \"#\") and put bot token here. You can create your app / bot and get your token here: " + Values.DISCORD_GET_TOKEN);
		defaults.put(ADMINS_ROLE, "Admins #comment out (add \"#\") or delete everything behind \"=\" to disable");
		defaults.put(CONTROL_CHANNEL, "bot");
		defaults.put(COMMAND_PREFIX, "!");
		defaults.put(VOICE_CHANNEL, "Music");
		defaults.put(DISPLAY_SONG_AS_GAME, "true");
		defaults.put(UPDATE_CHECK_INTERVAL_HOURS, "24 #set to 0 to disable");
		defaults.put(VOLUME, "100");

		JSONObject read;
		try {
			read = new JSONObject(new JSONTokener(new FileReader(file)));
		} catch (Exception e) {
			// Failed to load config. Maybe it doesn't exist (already).
			// Create an empty one
			read = new JSONObject();
		}

		ArrayList<String> toAdd = null;
		Iterator<String> keys = defaults.keys();
		while(keys.hasNext()) {
			String key = keys.next();
			if(!read.has(key)) {
				if(toAdd == null) {
					toAdd = new ArrayList<>();
				}
				toAdd.add(key);
			}
		}

		json = read;

		if(toAdd == null) {
			initialized = true;
		} else {
			if(generate(toAdd)) {
				String gotGeneratedOrUpdatedMSG = "Config file got generated or updated.";
				if(fromConfigGUI) {
					GUI.showMsgBox(gotGeneratedOrUpdatedMSG);
					initialized = true;
				} else {
					a.errExit(gotGeneratedOrUpdatedMSG + " Please edit it now.");
				}
			} else {
				a.errExit("Failed to generate config. (Do you have write access here?)");
			}
		}

		return initialized;
	}

	static String get(String key) {
		return toValue(json.getString(key));
	}

	static void set(String key, String value) {
		setRaw(key, toValue(value));
	}

	private static void setRaw(String key, String value) {
		json.put(key, value);
	}

	private static boolean generate(ArrayList<String> toAdd) {
		for(String key : toAdd) {
			setRaw(key, defaults.getString(key));
		}

		return save();
	}

	static boolean save() {
		try {
			json.write(new FileWriter(file), 2, 0).close();
			return true;
		} catch (Exception e) {
			//e.printStackTrace();
			return false;
		}
	}

	private static String toValue(String v) {
		return v.split("#")[0].trim();
	}

	static File getDefaultConfig() {
		if(DEFAULT_CONFIG != null) {
			return DEFAULT_CONFIG;
		}

		String os = System.getProperty("os.name").toLowerCase();
		if(os.equals("linux")) {
			DEFAULT_CONFIG = new File(System.getProperty("user.home"), ("." + Values.BOT_NAME.toLowerCase() + ".json"));
		} else if(os.startsWith("windows")) { //TODO: Test
			//TODO: Test
			DEFAULT_CONFIG = new File(System.getenv("APPDATA"), (Values.BOT_NAME.toLowerCase() + ".json"));
		} else {
			DEFAULT_CONFIG = new File("config.json");
		}

		return DEFAULT_CONFIG;
	}

}