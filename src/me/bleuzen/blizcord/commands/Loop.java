package me.bleuzen.blizcord.commands;

import me.bleuzen.blizcord.AudioPlayerThread;
import me.bleuzen.blizcord.Utils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Loop extends Command {

	@Override
	public String getName() {
		return "loop";
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		if(!Utils.isAdmin(author)) {
			channel.sendMessage(author.getAsMention() + " ``Only admins can enable loop.``").queue();
			return;
		}

		if(AudioPlayerThread.loop) {
			AudioPlayerThread.loop = false;
			channel.sendMessage(author.getAsMention() + " ``Looping disabled.``").queue();
		} else {
			AudioPlayerThread.loop = true;
			channel.sendMessage(author.getAsMention() + " ``Looping enabled.``").queue();
		}
	}

}
