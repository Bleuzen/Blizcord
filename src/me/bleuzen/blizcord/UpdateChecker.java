package me.bleuzen.blizcord;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.TimerTask;

import me.bleuzen.blizcord.bot.Bot;
import me.bleuzen.blizcord.gui.GUI;

public class UpdateChecker extends TimerTask {

	private final String GITHUB_REPO;

	private String json;

	private boolean updateAvailable = false;
	//private boolean alreadyNotified = false;

	public UpdateChecker(String githubRepo) {
		GITHUB_REPO = githubRepo;
	}

	private void checkForUpdate() {
		Log.debug("[Updater] Checking for updates ...");

		try {
			InputStream inputStream = new URL("https://api.github.com/repos/" + GITHUB_REPO + "/releases/latest").openStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			json = bufferedReader.readLine();

			String tagName = getJsonValue("tag_name");

			String online = tagName;
			String local = Values.BOT_VERSION;

			updateAvailable = compare(local, online);
		} catch (Exception e) {
			Log.warn("Failed to check for updates.");
			e.printStackTrace();
		}
	}

	private String getJsonValue(final String key) throws Exception {
		int i = json.indexOf(key);
		if(i == -1) { // json doesn't contain the key
			throw new Exception();
		}
		return json.substring(i + key.length() + 3).split("\"")[0];
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

		Log.debug("[Updater] Newer: {}", newer);

		return newer;
	}

	public boolean isUpdateAvailable() {
		return updateAvailable;
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
				GUI.showUpdatePanel();
			}
		}
	}

}