package me.bleuzen.blizcord;

import java.awt.Desktop;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

public class Utils {

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
		if(sourceName.equals("local") || sourceName.equals("http")) {
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

	static void openInBrowser(String link) {
		try {
			Desktop.getDesktop().browse(new URI(link));
		} catch (Exception e) {
			GUI.showErrMsgBox(e.getMessage());
			errExit();
		}
	}

	static void errExit() {
		errExit(null);
	}

	static void errExit(String msg) {
		errExit(msg, 1);
	}


	static void errExit(String msg, int exitCode) {
		if(a.isGui()) {
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

		// returns what is behind an argument
		public static String getArg(String[] args, String arg) {
			String result = null; // return null if argument is not given
			int i = getArgIndex(args, arg);
			if(i != -1) {
				try {
					result = args[i + 1];
				} catch(ArrayIndexOutOfBoundsException e) {
					Log.debug("Invalid argument value: {}", arg);
				}
			}
			return result;
		}

	}


}
