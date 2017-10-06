package me.bleuzen.blizcord.commands;

import me.bleuzen.blizcord.bot.AudioPlayerThread;
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

		AudioPlayerThread.addToPlaylist(arg);
	}

}
