package me.bleuzen.blizcord.commands;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;

import me.bleuzen.blizcord.Bot;
import me.bleuzen.blizcord.Values;
import me.bleuzen.blizcord.a;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class About extends Command {

	@Override
	public String getName() {
		return "about";
	}

	@Override
	public boolean isAdminOnly() {
		return false;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		channel.sendMessage("__**" + Values.BOT_NAME + "**__\n\n"
				+ "Version: " + Values.BOT_VERSION + "\n"
				+ "Author: " + Values.BOT_DEVELOPER + "\n"
				+ "GitHub: https://github.com/" + Values.BOT_GITHUB_REPO
				+ "\n\n"
				+ "__**Dependencies**__\n\n"
				+ "__JDA__\n"
				+ "Version: " + JDAInfo.VERSION + "\n"
				+ "GitHub: https://github.com/DV8FromTheWorld/JDA\n"
				+ "__Lavaplayer__\n"
				+ "Version: " + PlayerLibrary.VERSION + "\n"
				+ "GitHub: https://github.com/sedmelluq/lavaplayer\n"
				+ "__JNativeHook__\n"
				+ "GitHub: https://github.com/kwhat/jnativehook\n"
				+ "__Logback__\n"
				+ "GitHub: https://github.com/qos-ch/logback").queue();


		if(!a.isDisableUpdateChecker() && Bot.getUpdateChecker().isUpdateAvailable()) {
			Bot.sendUpdateMessage(false);
		}
	}

}
