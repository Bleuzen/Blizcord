import java.awt.GraphicsEnvironment;
import java.io.File;

import javax.swing.UIManager;

import org.jnativehook.GlobalScreen;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import net.dv8tion.jda.core.utils.SimpleLog;
import net.dv8tion.jda.core.utils.SimpleLog.Level;

public class a {

	private static boolean gui;
	private static boolean debug;
	private static boolean disableUpdateChecker; // for AUR users (currently only disabled in GUI)
	private static File configFile;

	static boolean isGui() {
		return gui;
	}

	static boolean isDisableUpdateChecker() {
		return disableUpdateChecker;
	}

	public static void main(String[] args) {
		gui = containsArg(args, "--gui");
		debug = containsArg(args, "--debug") || Values.DEV;
		disableUpdateChecker = containsArg(args, "--disable-update-checker");

		setupLogging();

		if(gui) {
			if(GraphicsEnvironment.isHeadless()) {
				// no gui supported
				gui = false; // disable gui mode
				errExit("GUI is not supported on your system.", Values.EXIT_CODE_GUI_NOT_SUPPORTED);
			}
			try {
				try {
					if(System.getProperty("os.name").toLowerCase().equals("linux") && System.getenv("XDG_CURRENT_DESKTOP").toLowerCase().equals("kde")) {
						// KDE theme fix
						UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
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
				errExit("Failed to start GUI: " + e.getMessage());
			}
		} else {
			launch(args);
		}
	}

	static void launch(String[] args) {
		Log.info("Version: " + Values.BOT_VERSION);
		Log.info("Developer: " + Values.BOT_DEVELOPER);

		Log.info("Starting bot ...");

		// init config
		String configArg = getArg(args, "--config");
		if(configArg != null) {
			configFile = new File(configArg);
		} else {
			configFile = Config.getDefaultConfig();
		}

		Log.info("Config: " + configFile.getAbsolutePath());

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
		errExit(msg, 1);
	}


	static void errExit(String msg, int exitCode) {
		if(gui) {
			GUI.onErrExit(msg);
		} else {
			Log.error("Crash! Reason:");
			System.err.println(msg == null ? "Unknown" : msg);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}

		if(exitCode >= 1 && exitCode <= 127) {
			System.exit(exitCode);
		} else {
			System.exit(0);
		}
	}

	private static int getArgIndex(String[] args, String arg) {
		int ir = -1; // return -1 if args does not contain the argument
		for(int in = 0; in < args.length; in++) {
			if(args[in].equalsIgnoreCase(arg)) {
				ir = in;
				break;
			}
		}
		return ir;
	}

	private static boolean containsArg(String[] args, String arg) {
		return getArgIndex(args, arg) != -1;
	}

	// returns what is behind an argument
	private static String getArg(String[] args, String arg) {
		String result = null; // return null if argument is not given
		int i = getArgIndex(args, arg);
		if(i != -1) {
			result = args[i + 1];
		}
		return result;
	}

	private static void setupLogging() {
		Logger lavaplayerLogger = (Logger) LoggerFactory.getLogger("com.sedmelluq.discord.lavaplayer");
		java.util.logging.Logger jNativeHookLogger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());

		if(debug) {
			// set JDA logging to DEBUG
			SimpleLog.LEVEL = Level.DEBUG;
			// set lavaplayer logging to DEBUG
			lavaplayerLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
			// set JNativeHook logging to WARNING
			jNativeHookLogger.setLevel(java.util.logging.Level.WARNING);
		} else {
			if(gui) {
				// disable JDA logging
				SimpleLog.LEVEL = Level.OFF;
				// disable lavaplayer logging
				lavaplayerLogger.setLevel(ch.qos.logback.classic.Level.OFF);
				// disable JNativeHook logging
				jNativeHookLogger.setLevel(java.util.logging.Level.OFF);
			} else {
				// set JDA logging to WARNING
				SimpleLog.LEVEL = Level.WARNING;
				// set lavaplayer logging to WARN
				lavaplayerLogger.setLevel(ch.qos.logback.classic.Level.WARN);
				// set JNativeHook logging to WARNING
				jNativeHookLogger.setLevel(java.util.logging.Level.WARNING);
			}
		}


		// init own Logger
		Log.init(debug);

		// Print first log message
		Log.debug("Logger initialized.");
	}

}
