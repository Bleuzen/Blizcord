package com.github.bleuzen.blizcord.bot.commands;

import com.github.bleuzen.blizcord.Values;
import com.github.bleuzen.blizcord.a;
import com.github.bleuzen.blizcord.bot.Bot;
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;

import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

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
				+ "Source: https://github.com/" + Values.BOT_GITHUB_REPO
				+ "\n\n"
				+ "__**Dependencies**__\n\n"
				+ "__JDA__\n"
				+ "Version: " + JDAInfo.VERSION + "\n"
				+ "Source: https://github.com/DV8FromTheWorld/JDA\n"
				+ "__Lavaplayer__\n"
				+ "Version: " + PlayerLibrary.VERSION + "\n"
				+ "Source: https://github.com/sedmelluq/lavaplayer\n"
				+ "__JNativeHook__\n"
				+ "Source: https://github.com/kwhat/jnativehook\n"
				+ "__Logback__\n"
				+ "Source: https://github.com/qos-ch/logback").queue();


		if(!a.isDisableUpdateChecker() && Bot.getUpdateChecker().isUpdateAvailable()) {
			Bot.sendUpdateMessage(false);
		}
	}

}
