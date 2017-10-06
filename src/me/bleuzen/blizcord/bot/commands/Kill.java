package me.bleuzen.blizcord.bot.commands;

import me.bleuzen.blizcord.bot.Bot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Kill extends Command {

	@Override
	public String getName() {
		return "kill";
	}

	@Override
	public boolean isAdminOnly() {
		return true;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		channel.sendMessage("Bye").complete(); // complete(): block this thread (send the message first, than shutdown)
		Bot.shutdown();
	}

}
