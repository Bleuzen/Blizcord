package com.github.bleuzen.blizcord;
import java.io.InputStream;
import java.net.URL;
import java.util.TimerTask;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.github.bleuzen.blizcord.bot.Bot;
import com.github.bleuzen.blizcord.gui.GUI_Main;

public class UpdateChecker extends TimerTask {

	private static final String UPDATER_URL = "https://gitlab.com/Bleuzen/versions/raw/master/blizcord/v1/data.json";

	private boolean updateAvailable = false;
	
	private String latestVersion = null;
	private String downloadUrl = null;

	private void checkForUpdate() {
		Log.info("[Updater] Checking for updates...");

		try {
			JSONObject json = fetchJSON(UPDATER_URL);

			latestVersion = json.getString("version");
			downloadUrl = json.getString("downloadUrl");

			Log.debug("[Updater] [JSON] online version: " + latestVersion);

			final String online = latestVersion;
			final String local = Values.BOT_VERSION;

			updateAvailable = compare(local, online);
		} catch (Exception e) {
			Log.warn("Failed to check for updates.");
			e.printStackTrace();
		}
	}

	private JSONObject fetchJSON(String url) throws Exception {
		InputStream inputStream = new URL(url).openStream();
		JSONObject json = new JSONObject(new JSONTokener(inputStream));
		inputStream.close();
		return json;
	}

	private String toVersionString(String version) {
		return version.replaceAll("[^0-9.]", "");
	}

	private boolean compare(String local, String online) {
		local = toVersionString(local);
		online = toVersionString(online);

		Log.debug("[Updater] Online version: {}", online);

		String[] valsLocal = local.split("\\.");
		String[] valsOnline = online.split("\\.");

		// find the first non-equal number
		int i = 0;
		while(i < valsLocal.length && i < valsOnline.length && valsLocal[i].equals(valsOnline[i])) {
			i++;
		}

		boolean newer;
		if(i < valsLocal.length && i < valsOnline.length) {
			int numLocal = Integer.parseInt(valsLocal[i]);
			int numOnline = Integer.parseInt(valsOnline[i]);
			newer = numOnline > numLocal;
		} else {
			newer = valsOnline.length > valsLocal.length;
		}

		if (newer) {
			Log.info("[Updater] An update is available!");
		} else {
			Log.info("[Updater] No update available.");
		}

		return newer;
	}

	public boolean isUpdateAvailable() {
		return updateAvailable;
	}

	public String getLatestVersion() {
		return latestVersion;
	}
	
	public String getDownloadUrl() {
		return downloadUrl;
	}

	@Override
	public void run() {
		// Check for update if needed
		if(!updateAvailable) {
			checkForUpdate();
		}

		// Notify the user
		if(updateAvailable) {
			// Send update message to server owner
			Bot.sendUpdateMessage(true);

			// Display in GUI
			if(a.isGui()) {
				GUI_Main.showUpdatePanel();
			}
		}
	}

}