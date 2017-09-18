package me.bleuzen.blizcord.commands;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class Lists extends Command {

	@Override
	public String getName() {
		return "lists";
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		//TODO: list all saved playlists
	}

}
