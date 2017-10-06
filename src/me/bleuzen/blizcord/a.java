package me.bleuzen.blizcord;
import java.awt.GraphicsEnvironment;

import javax.swing.UIManager;

import me.bleuzen.blizcord.Utils.ArgumentUtils;
import me.bleuzen.blizcord.bot.Bot;

public class a {

	private static boolean gui;
	private static boolean debug;

	/*
	 * > Mainly for AUR users
	 * - disable checkbox in GUI_Config
	 * - not check for updates
	 * */
	private static boolean disableUpdateChecker;

	public static boolean isGui() {
		return gui;
	}

	static boolean isDebug() {
		return debug;
	}

	public static boolean isDisableUpdateChecker() {
		return disableUpdateChecker;
	}

	public static void main(String[] args) {
		debug = ArgumentUtils.containsArg(args, "--debug") || Values.DEV;
		Log.init();

		gui = ArgumentUtils.containsArg(args, "--gui");
		disableUpdateChecker = ArgumentUtils.containsArg(args, "--disable-update-checker");

		Log.info("Version: " + Values.BOT_VERSION);
		Log.info("Developer: " + Values.BOT_DEVELOPER);

		if(Utils.isUnknownOS()) {
			Log.warn("Your operating system is not supported yet. Some features may not work.");
		}

		if(gui) {

			if(GraphicsEnvironment.isHeadless()) {
				// no gui supported
				gui = false; // disable gui mode
				Utils.errExit("GUI is not supported on your system.");
			}

			try {

				try {
					// Use the systems theme
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

					// Linux Fixes
					if(Utils.getOS().equals(Values.OS_LINUX)) {
						// Linux Font fix
						// https://wiki.archlinux.org/index.php/Java_Runtime_Environment_fonts
						System.setProperty("awt.useSystemAAFontSettings", "gasp");

						// KDE theme fix
						if(System.getenv("XDG_CURRENT_DESKTOP").toLowerCase().equals("kde")) {
							UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
						}
					}
				} catch (Exception e) {
					Log.debug("Failed to set look and feel.");
				}

				Log.debug("Launching GUI ...");

				// Launch GUI
				GUI frame = new GUI();
				frame.setVisible(true);
			} catch (Exception e) {
				Utils.errExit("Failed to start GUI: " + e.getMessage());
			}

		} else {
			Bot.launch(args);
		}

	}

}
