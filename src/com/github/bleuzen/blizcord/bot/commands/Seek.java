package com.github.bleuzen.blizcord.bot.commands;

import com.github.bleuzen.blizcord.Utils;
import com.github.bleuzen.blizcord.bot.AudioPlayerThread;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

class Seek extends Command {

	@Override
	public String getName() {
		return "seek";
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

		if(arg == null) {
			channel.sendMessage(author.getAsMention() + " ``Please specify a time. Put it behind this command. Split hours, minutes and seconds with ':'. Hours and minutes are optional.``").queue();
			return;
		}

		long ms = -1; // invalid by default
		try {
			int c = arg.length() - arg.replace(":", "").length();
			if(c == 2) {
				// hours, minutes and seconds
				String[] split = arg.split(":");
				ms = Utils.timeToMS(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
			} else if(c == 1) {
				// minutes and seconds
				String[] split = arg.split(":");
				ms = Utils.timeToMS(0, Integer.parseInt(split[0]), Integer.parseInt(split[1]));
			} else if(c == 0) {
				// only seconds
				ms = Utils.timeToMS(0, 0, Integer.parseInt(arg));
			}

			if(ms < 0) {
				throw new NumberFormatException();
			}
		} catch(Exception e) {
			channel.sendMessage(author.getAsMention() +  " ``Invalid time``").queue();
			return;
		}

		AudioPlayerThread.getMusicManager().player.getPlayingTrack().setPosition(ms);
	}

}
