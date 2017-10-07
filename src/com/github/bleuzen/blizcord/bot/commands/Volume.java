package com.github.bleuzen.blizcord.bot.commands;

import com.github.bleuzen.blizcord.Values;
import com.github.bleuzen.blizcord.bot.AudioPlayerThread;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Volume extends Command {

	@Override
	public String getName() {
		return "volume";
	}

	@Override
	public boolean isAdminOnly() {
		return true;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		if(arg == null) {
			channel.sendMessage(author.getAsMention() + " Please write the new volume behind this command.").queue();
			return;
		}

		int r = AudioPlayerThread.setVolume(arg);

		switch(r) {
		case Values.SET_VOLUME_SUCCESSFULLY:
			channel.sendMessage(author.getAsMention() + " Volume set to: " + arg + "%").queue();
			break;

		case Values.SET_VOLUME_ERROR_CUSTOM_VOLUME_NOT_ALLOWED:
			channel.sendMessage(author.getAsMention() + " *Allow custom volume* is disabled in your configuration.").queue();
			break;

		case Values.SET_VOLUME_ERROR_INVALID_NUMBER:
			channel.sendMessage(author.getAsMention() + " Invalid input. Only numbers between 0 and 100 are allowed.").queue();
			break;
		}

	}

}
