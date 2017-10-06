package me.bleuzen.blizcord.bot.commands;

import me.bleuzen.blizcord.Utils;
import me.bleuzen.blizcord.bot.Bot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Uptime extends Command {

	@Override
	public String getName() {
		return "uptime";
	}

	@Override
	public boolean isAdminOnly() {
		return false;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		long duration = System.currentTimeMillis() - Bot.getStartTime();
		channel.sendMessage(author.getAsMention() + " ``Uptime: " + Utils.durationToTimeString(duration) + "``").queue();
	}

}
