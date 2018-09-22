package com.github.bleuzen.blizcord.bot.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Loadshuffle extends Command {

	@Override
	public String getName() {
		return "loadshuffle";
	}

	@Override
	public boolean isAdminOnly() {
		return true;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		// arg = playlist
		Load.loadPlaylist(arg, author, channel, true);
	}

}
