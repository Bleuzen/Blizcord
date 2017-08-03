package me.bleuzen.blizcord.commands;

import me.bleuzen.blizcord.Bot;
import me.bleuzen.blizcord.Utils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Kill extends Command {

	@Override
	public String getName() {
		return "kill";
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		if(Utils.isAdmin(author)) {
			channel.sendMessage("Bye").complete(); // complete(): block this thread (send the message first, than shutdown)
			Bot.shutdown();
		} else {
			channel.sendMessage(author.getAsMention() + " ``Only admins can kill me.``").queue();
		}
	}

}
