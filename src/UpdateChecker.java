import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.TimerTask;

public class UpdateChecker extends TimerTask {

	private String json;

	private boolean updateAvailable = false;
	private boolean alreadyNotified = false;

	private void checkForUpdate() {
		Log.debug("[Updater] Checking for updates ...");

		try {
			InputStream inputStream = new URL("https://api.github.com/repos/" + Values.BOT_GITHUB_REPO + "/releases/latest").openStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
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

		Log.debug("[Updater] Local version: {}", local);
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

	boolean isUpdateAvailable() {
		return updateAvailable;
	}

	@Override
	public void run() {
		if(!alreadyNotified) {
			checkForUpdate();
			if(updateAvailable) {
				Bot.sendUpdateMessage(true);
				alreadyNotified = true;
			}
		}
	}

}
