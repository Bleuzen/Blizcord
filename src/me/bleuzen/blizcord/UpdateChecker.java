package me.bleuzen.blizcord;
import java.io.InputStream;
import java.net.URL;
import java.util.TimerTask;

import org.json.JSONObject;
import org.json.JSONTokener;

import me.bleuzen.blizcord.bot.Bot;
import me.bleuzen.blizcord.gui.GUI;

public class UpdateChecker extends TimerTask {

	private final String GITHUB_REPO;

	private boolean updateAvailable = false;

	public UpdateChecker(String githubRepo) {
		GITHUB_REPO = githubRepo;
	}

	private void checkForUpdate() {
		Log.info("[Updater] Checking for updates ...");

		try {
			InputStream inputStream = new URL("https://api.github.com/repos/" + GITHUB_REPO + "/releases/latest").openStream();
			JSONObject json = new JSONObject(new JSONTokener(inputStream));

			inputStream.close();

			String tagName = json.getString("tag_name");
			boolean prerelease = json.getBoolean("prerelease");

			Log.debug("[Updater] [JSON] tag_name: " + tagName);
			Log.debug("[Updater] [JSON] prerelease: " + prerelease);

			if(prerelease) {
				updateAvailable = false;
				Log.debug("[Updater] Skipping version comparison");
				return;
			}

			String online = tagName;
			String local = Values.BOT_VERSION;

			updateAvailable = compare(local, online);
		} catch (Exception e) {
			Log.warn("Failed to check for updates.");
			e.printStackTrace();
		}
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