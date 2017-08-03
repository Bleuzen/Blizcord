package me.bleuzen.blizcord;

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

}
