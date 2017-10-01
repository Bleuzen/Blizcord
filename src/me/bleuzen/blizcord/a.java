package me.bleuzen.blizcord;
import java.awt.GraphicsEnvironment;

import javax.swing.UIManager;

import me.bleuzen.blizcord.Utils.ArgumentUtils;

public class a {

	private static boolean gui;
	private static boolean debug;

	/*
	 * > Mainly for AUR users
	 * - disable checkbox in GUI_Config
	 * - not check for updates
	 * */
	private static boolean disableUpdateChecker;

	static boolean isGui() {
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

		if(gui) {

			if(GraphicsEnvironment.isHeadless()) {
				// no gui supported
				gui = false; // disable gui mode
				Utils.errExit("GUI is not supported on your system.");
			}
			try {
				try {
					if(System.getProperty("os.name").toLowerCase().equals("linux")) {
						// Linux Font fix
						// https://wiki.archlinux.org/index.php/Java_Runtime_Environment_fonts
						System.setProperty("awt.useSystemAAFontSettings", "gasp");

						// KDE theme fix
						if(System.getenv("XDG_CURRENT_DESKTOP").toLowerCase().equals("kde")) {
							UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
						}
					} else {
						// Use the systems theme
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
