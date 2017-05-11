import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.TimerTask;

public class UpdateChecker extends TimerTask {

	private String json;
	private String tagName;

	private boolean updateAvailable = false;
	private boolean alreadyNotified = false;

	private void checkForUpdate() {
		try {
			InputStream inputStream = new URL("https://api.github.com/repos/" + Values.BOT_GITHUB_REPO + "/releases/latest").openStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
			json = bufferedReader.readLine();

			tagName = getJsonValue("tag_name");

			int online = toVersionNumber(tagName);
			int local = toVersionNumber(Values.BOT_VERSION);

			if(online > local) {
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

	private int toVersionNumber(String version) {
		return Integer.parseInt(version.replaceAll("[^0-9]", ""));
	}

	@Override
	public void run() {
		if(!alreadyNotified) {
			checkForUpdate();
			if(updateAvailable) {
				Bot.getControlChannel().sendMessage("A new version is available!\n"
						+ "https://github.com/" + Values.BOT_GITHUB_REPO + "/releases/tag/" + tagName).queue();
				alreadyNotified = true;
			}
		}
	}

}
