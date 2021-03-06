package com.github.bleuzen.blizcord.bot.commands;

import java.io.File;

import com.github.bleuzen.blizcord.Config;
import com.github.bleuzen.blizcord.Values;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

class Lists extends Command {

	@Override
	public String getName() {
		return "lists";
	}

	@Override
	public boolean isAdminOnly() {
		return true;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		File playlistsFolder = new File(Config.getAppDir(), "playlists");
		if(!playlistsFolder.isDirectory()) {
			channel.sendMessage(author.getAsMention() + " ``There are no playlists.``").queue();
			return;
		}

		File[] playlists = playlistsFolder.listFiles();
		if(playlists.length == 0) {
			channel.sendMessage(author.getAsMention() + " ``There are no playlists.``").queue();
			return;
		}

		StringBuilder outputMsg = new StringBuilder(author.getAsMention() + " ``Available playlists:``\n"
				+ "```\n");
		for(File playlist : playlists) {
			outputMsg.append(playlist.getName() + "\n");
		}
		outputMsg.append("```");

		if(outputMsg.length() > Values.MAX_MESSAGE_LENGTH) {
			final String ending = "...```";
			outputMsg.setLength((Values.MAX_MESSAGE_LENGTH - ending.length()));
			outputMsg.append(ending);
		}
		channel.sendMessage(outputMsg.toString()).queue();
	}

}
