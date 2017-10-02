package me.bleuzen.blizcord.commands;

import me.bleuzen.blizcord.AudioPlayerThread;
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