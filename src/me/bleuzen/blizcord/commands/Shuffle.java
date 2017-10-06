package me.bleuzen.blizcord.commands;

import me.bleuzen.blizcord.bot.AudioPlayerThread;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

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
