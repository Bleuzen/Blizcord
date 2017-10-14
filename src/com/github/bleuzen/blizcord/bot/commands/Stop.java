package com.github.bleuzen.blizcord.bot.commands;

import com.github.bleuzen.blizcord.bot.Bot;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Stop extends Command {

	@Override
	public String getName() {
		return "stop";
	}

	@Override
	public boolean isAdminOnly() {
		return true;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		Bot.stopPlayer();

		channel.sendMessage("``Playback stopped.``").queue();
	}

}
