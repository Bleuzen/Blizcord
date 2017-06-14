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

			String online = toVersionString(tagName);
			String local = toVersionString(Values.BOT_VERSION);

			Log.debug("[Updater] Online version: " + online);
			Log.debug("[Updater] Local version: " + local);

			int t = online.compareTo(local);

			Log.debug("[Updater] Compare result: " + t);

			if(t > 0) {
				updateAvailable = true;
			}
		} catch (Exception e) {
			Log.print("Failed to check for updates.");
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
		return version.replaceAll("[^0-9]", "");
	}

	boolean isUpdateAvailable() {
		return updateAvailable;
	}

	@Override
	public void run() {
		if(!alreadyNotified) {
			checkForUpdate();
			if(updateAvailable) {
				Bot.sendUpdateMessage();
				alreadyNotified = true;
			}
		}
	}

}
