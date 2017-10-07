package com.github.bleuzen.blizcord.bot.commands;

import com.github.bleuzen.blizcord.bot.AudioPlayerThread;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class List extends Command {

	@Override
	public String getName() {
		return "list";
	}

	@Override
	public boolean isAdminOnly() {
		return false;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		AudioPlayerThread.sendPlaylist(author, channel);
	}

}
