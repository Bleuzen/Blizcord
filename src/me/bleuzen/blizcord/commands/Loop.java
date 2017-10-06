package me.bleuzen.blizcord.commands;

import me.bleuzen.blizcord.bot.AudioPlayerThread;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Loop extends Command {

	@Override
	public String getName() {
		return "loop";
	}

	@Override
	public boolean isAdminOnly() {
		return true;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		if(AudioPlayerThread.loop) {
			AudioPlayerThread.loop = false;
			channel.sendMessage(author.getAsMention() + " ``Looping disabled.``").queue();
		} else {
			AudioPlayerThread.loop = true;
			channel.sendMessage(author.getAsMention() + " ``Looping enabled.``").queue();
		}
	}

}
