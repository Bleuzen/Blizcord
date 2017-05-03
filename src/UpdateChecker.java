import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

public class UpdateChecker implements Runnable {
	
	private String json;
	
	private void checkForUpdate() {
		try {
			InputStream inputStream = new URL("https://api.github.com/repos/" + Values.BOT_GITHUB_REPO + "/releases/latest").openStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
			json = bufferedReader.readLine();
			
			String tagName = getJsonValue("tag_name");
			int online = toVersionNumber(tagName);
			int local = toVersionNumber(Values.BOT_VERSION);
			
			if(online > local) {
				Log.print("A new version is available!");	
				Log.print("https://github.com/" + Values.BOT_GITHUB_REPO + "/releases/tag/" + tagName);
			} else {
				Log.print("You are using the latest version.");
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
		checkForUpdate();
	}
	
}
