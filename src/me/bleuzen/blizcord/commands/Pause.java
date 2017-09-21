package me.bleuzen.blizcord.commands;

import me.bleuzen.blizcord.AudioPlayerThread;
import me.bleuzen.blizcord.Utils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Pause extends Command {

	@Override
	public String getName() {
		return "pause";
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		if(!Utils.isAdmin(author)) {
			channel.sendMessage(author.getAsMention() + " ``Only admins can pause me.``").queue();
			return;
		}

		if(AudioPlayerThread.isPaused()) {
			channel.sendMessage("``Continue playback ...``").queue();
			AudioPlayerThread.setPaused(false);
		} else {
			AudioPlayerThread.setPaused(true);
			channel.sendMessage("``Paused.\n"
					+ "Type this command again to resume.``").queue();
		}
	}

}
