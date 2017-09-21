package me.bleuzen.blizcord.commands;

import me.bleuzen.blizcord.AudioPlayerThread;
import me.bleuzen.blizcord.Utils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Shuffle extends Command {

	@Override
	public String getName() {
		return "shuffle";
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		if(!Utils.isAdmin(author)) {
			channel.sendMessage(author.getAsMention() + " ``Only admins can shuffle the playlist.``").queue();
			return;
		}

		AudioPlayerThread.getMusicManager().scheduler.shuffle();

		channel.sendMessage(author.getAsMention() + " ``The playlist got shuffeled.``").queue();
	}

}
