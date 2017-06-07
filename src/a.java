import java.awt.Desktop;
import java.io.File;
import java.net.URI;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class a {

	private static boolean gui;

	static boolean isGui() {
		return gui;
	}

	public static void main(String[] args) {
		if(args.length > 0 && args[0].equalsIgnoreCase("--gui")) {
			gui = true;
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				GUI frame = new GUI();
				frame.setVisible(true);
			} catch (Exception e) {
				System.out.print("Failed to start GUI: " + e.getMessage());
				errExit();
			}
		} else {
			launch(args);
		}
	}

	static void launch(String[] args) {
		Log.print("Version: " + Values.BOT_VERSION);
		Log.print("Developer: " + Values.BOT_DEVELOPER);

		Log.print("Starting ...");

		// init config
		File configFile;
		if(Values.TESTING) {
			configFile = new File("testingConfig.json");
		} else {
			if(args.length > 1 && args[0].equalsIgnoreCase("--config")) {
				configFile = new File(args[1]);
			} else {
				configFile = new File(Values.DEFAULT_CONFIG);
			}
		}

		Log.print("Config: " + configFile.getAbsolutePath());

		// load the config file
		if(!Config.init(configFile)) {
			errExit("Failed to load config.");
		}

		// Start the bot
		Bot.start();
	}

	static void errExit() {
		errExit(null);
	}

	static void errExit(String msg) {
		if(gui) {
			GUI.onErrExit(msg);
		} else {
			Log.print("Crash! Reason:");
			System.err.println(msg == null ? "Unknown" : msg);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
		System.exit(1);
	}

	//TODO: maybe find a better way (move to GUI class?)
	static void addToServerMessage(String link) {
		if(gui) {
			int r = JOptionPane.showConfirmDialog(null, "Do you want to add the bot to your server now?" + System.lineSeparator() + "This will open:" + System.lineSeparator() + link, Values.BOT_NAME, JOptionPane.YES_NO_OPTION);
			if(r == JOptionPane.YES_OPTION) {
				try {
					Desktop.getDesktop().browse(new URI(link));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.exit(1); // exit / restart after invite
		} else {
			errExit("To add the bot to your server visit: " + link);
		}
	}

}
