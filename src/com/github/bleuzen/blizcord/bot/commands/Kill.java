package com.github.bleuzen.blizcord.bot.commands;

import com.github.bleuzen.blizcord.bot.Bot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

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
		channel.sendMessage("``Bye!``").complete(); // complete(): block this thread (send the message first, than shutdown)
		Bot.shutdown();
	}

}
