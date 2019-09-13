package com.github.bleuzen.blizcord.bot.commands;

import com.github.bleuzen.blizcord.Utils;
import com.github.bleuzen.blizcord.bot.Bot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

class Uptime extends Command {

	@Override
	public String getName() {
		return "uptime";
	}

	@Override
	public boolean isAdminOnly() {
		return false;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		long duration = System.currentTimeMillis() - Bot.getStartTime();
		channel.sendMessage(author.getAsMention() + " Uptime: ``" + Utils.durationToUpTimeString(duration) + "``").queue();
	}

}
