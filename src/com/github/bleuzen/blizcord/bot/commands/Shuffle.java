package com.github.bleuzen.blizcord.bot.commands;

import com.github.bleuzen.blizcord.bot.AudioPlayerThread;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

class Shuffle extends Command {

	@Override
	public String getName() {
		return "shuffle";
	}

	@Override
	public boolean isAdminOnly() {
		return true;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		AudioPlayerThread.getMusicManager().scheduler.shuffle();

		channel.sendMessage(author.getAsMention() + " ``The playlist got shuffeled.``").queue();
	}

}
