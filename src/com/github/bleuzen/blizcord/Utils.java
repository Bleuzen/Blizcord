package com.github.bleuzen.blizcord;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import com.github.bleuzen.blizcord.bot.Bot;
import com.github.bleuzen.blizcord.gui.GUI_Main;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

public class Utils {

	private static String OS;

	public static String getOS() {
		if(OS != null) {
			return OS;
		}

		String tmp = System.getProperty("os.name").toLowerCase();
		if(tmp.equals(Values.OS_LINUX)) {
			return (OS = Values.OS_LINUX);
		} else if(tmp.startsWith(Values.OS_WINDOWS)) {
			return (OS = Values.OS_WINDOWS);
		} else {
			return (OS = Values.UNKNOWN_OS);
		}
	}

	public static boolean isUnknownOS() {
		return getOS().equals(Values.UNKNOWN_OS);
	}

	public static long timeToMS(int hours, int minutes, int seconds) {
		if(seconds > 59 || seconds < 0) {
			return -1;
		}
		if(minutes > 59 || minutes < 0) {
			return -1;
		}

		long s = (seconds + (60 * (minutes + (hours * 60))));
		return TimeUnit.SECONDS.toMillis(s);
	}

	public static String durationToTimeString(long duration) {
		TimeUnit scale = TimeUnit.MILLISECONDS;
		long days = scale.toDays(duration);
		duration -= TimeUnit.DAYS.toMillis(days);
		long hours = scale.toHours(duration);
		duration -= TimeUnit.HOURS.toMillis(hours);
		long minutes = scale.toMinutes(duration);
		duration -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = scale.toSeconds(duration);
		return String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);
	}

	public static String getTrackName(AudioTrack track) {
		String sourceName = track.getSourceManager().getSourceName();
		if(sourceName.equals("local")) {
			return new File(track.getInfo().uri).getName();
		} else if(sourceName.equals("http")) {
			return track.getIdentifier();
		} else {
			return track.getInfo().title;
		}
	}

	public static boolean isAdmin(User user) {
		Guild guild = Bot.getGuild();
		Role adminRole = Bot.getAdminRole();
		return user.getId().equals(guild.getOwner().getUser().getId()) || (adminRole != null && guild.getMember(user).getRoles().contains(adminRole));
	}

	public static void openInBrowser(String link) {
		try {
			Desktop.getDesktop().browse(new URI(link));
		} catch (Exception e) {
			GUI_Main.showErrMsgBox(e.getMessage());
			errExit();
		}
	}

	static void errExit() {
		errExit(null);
	}

	public static void errExit(String msg) {
		errExit(msg, 1);
	}

	public static void errExit(String msg, int exitCode) {
		if(a.isGui()) {
			GUI_Main.onErrExit(msg);
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

	public static void handleError(String m) {
		Utils.errExit(m);
	}

	// "NCG" = "Not Crash GUI"
	// this is the same as handleError(m) but does not crash if GUI
	public static void handleErrorNCG(String m) {
		if(a.isGui()) {
			// Only show Error message box, but don't exit
			GUI_Main.showErrMsgBox(m);
		} else {
			// If not using GUI -> The old way: crash
			//TODO: double checks if a.isGui() (again in errExit()), Write better method?
			Utils.errExit(m);
		}
	}

	public static void handleException(Exception e) {
		if(a.isDebug()) {
			e.printStackTrace();
		}

		handleError(e.getMessage());
	}

	// "NCG" = "Not Crash GUI"
	public static void handleExceptionNCG(Exception e) {
		if(a.isDebug()) {
			e.printStackTrace();
		}

		handleErrorNCG(e.getMessage());
	}

	public static void printException(Exception e) {
		if(a.isDebug()) {
			e.printStackTrace();
		}
	}


	public static class ArgumentUtils {

		public static int getArgIndex(String[] args, String arg) {
			int ir = -1; // return -1 if args does not contain the argument
			for(int in = 0; in < args.length; in++) {
				if(args[in].equalsIgnoreCase(arg)) {
					ir = in;
					break;
				}
			}
			return ir;
		}

		public static boolean containsArg(String[] args, String arg) {
			return getArgIndex(args, arg) != -1;
		}

		// returns the value behind an argument or null
		public static String getArg(String[] args, String arg) {
			int i = getArgIndex(args, arg);
			if(i != -1) {
				try {
					String result = args[i + 1];

					if(!result.startsWith("--")) {
						return result;
					}
				} catch(ArrayIndexOutOfBoundsException e) {
					Log.debug("Invalid argument value: {}", arg);
				}
			}

			return null;
		}

	}


}
