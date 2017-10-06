package me.bleuzen.blizcord.bot.commands;

import java.io.File;

import me.bleuzen.blizcord.Config;
import me.bleuzen.blizcord.Values;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

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

		if(outputMsg.length() > Values.MAX_MESSAGE_LENGHT) {
			final String ending = "...```";
			outputMsg.setLength((Values.MAX_MESSAGE_LENGHT - ending.length()));
			outputMsg.append(ending);
		}
		channel.sendMessage(outputMsg.toString()).queue();
	}

}
