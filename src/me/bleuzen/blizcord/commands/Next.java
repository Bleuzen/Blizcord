package me.bleuzen.blizcord.commands;

import me.bleuzen.blizcord.bot.AudioPlayerThread;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Next extends Command {

	@Override
	public String getName() {
		return "next";
	}

	@Override
	public boolean isAdminOnly() {
		return true;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		if(!AudioPlayerThread.isPlaying()) {
			channel.sendMessage(author.getAsMention() + " ``Currently I'm not playing.``").queue();
			return;
		}

		int skips;
		if (arg == null) {
			skips = 1;
		} else {
			try {
				skips = Integer.parseInt(arg);
				if (skips < 1) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException e) {
				channel.sendMessage(author.getAsMention() + " Invalid number").queue();
				return;
			}
		}

		AudioPlayerThread.getMusicManager().scheduler.nextTrack(skips);
	}

}
