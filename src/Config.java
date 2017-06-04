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

	private static JSONObject defaults;
	private static boolean initialized;

	private static File file;
	private static JSONObject json;

	static boolean init(File configFile) {
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

		if(toAdd == null) {
			json = read;
			initialized = true;
			return true;
		} else {
			json = new JSONObject();
			if(generate(toAdd)) {
				a.errExit("Config file got generated or updated. Please edit it now.");
			} else {
				a.errExit("Failed to generate config. (Do you have write access here?)");
			}
			return false; // will never get called, but Eclipse wants it
		}
	}

	static String get(String key) {
		return toValue(json.getString(key));
	}

	static void set(String key, String value) {
		json.put(key, toValue(value));
	}

	private static boolean generate(ArrayList<String> toAdd) {
		for(String key : toAdd) {
			set(key, defaults.getString(key));
		}

		//TODO: save in other order if possible (BOT_TOKEN at the top)

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

}