package me.bleuzen.blizcord.commands;

import me.bleuzen.blizcord.Bot;
import me.bleuzen.blizcord.Utils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Stop extends Command {

	@Override
	public String getName() {
		return "stop";
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		if(Utils.isAdmin(author)) {
			Bot.stopPlayer();
		} else {
			channel.sendMessage(author.getAsMention() + " ``Only admins can stop me.``").queue();
		}
	}

}
