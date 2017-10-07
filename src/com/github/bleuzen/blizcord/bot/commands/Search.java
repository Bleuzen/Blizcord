package com.github.bleuzen.blizcord.bot.commands;

import com.github.bleuzen.blizcord.Values;
import com.github.bleuzen.blizcord.bot.AudioPlayerThread;
import com.github.bleuzen.blizcord.bot.Bot;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Search extends Command {

	@Override
	public String getName() {
		return "search";
	}

	@Override
	public boolean isAdminOnly() {
		return false;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		if(arg == null) {
			channel.sendMessage(author.getAsMention() + " ``Please specify a video title. Put it behind this command.``").queue();
			return;
		}

		// Join the voice channel of the command author before addToPlaylist() joins the default channel
		Bot.joinVoiceChannel(Bot.getGuild().getMember(author));

		AudioPlayerThread.addToPlaylist((Values.SEARCH_PREFIX_YOUTUBE + arg), false); // uses the "ytsearch:" prefix of lavaplayer
	}

}
