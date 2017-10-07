package com.github.bleuzen.blizcord.bot.commands;

import com.github.bleuzen.blizcord.bot.AudioPlayerThread;
import com.github.bleuzen.blizcord.bot.Bot;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Add extends Command {

	@Override
	public String getName() {
		return "add";
	}

	@Override
	public boolean isAdminOnly() {
		return false;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		if(arg == null) {
			channel.sendMessage(author.getAsMention() + " ``Please specify what I should add to the playlist. Put it behind this command.``").queue();
			return;
		}

		// Join the voice channel of the command author before addToPlaylist() joins the default channel
		Bot.joinVoiceChannel(Bot.getGuild().getMember(author));

		AudioPlayerThread.addToPlaylist(arg, false);
	}

}
