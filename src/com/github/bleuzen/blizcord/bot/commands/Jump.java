package com.github.bleuzen.blizcord.bot.commands;

import com.github.bleuzen.blizcord.bot.AudioPlayerThread;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

class Jump extends Command {

	@Override
	public String getName() {
		return "jump";
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

		int seconds;
		if(arg == null) {
			seconds = 10;
		} else {
			try {
				seconds = Integer.parseInt(arg);
				if(seconds == 0) {
					throw new NumberFormatException();
				}
			} catch(NumberFormatException e) {
				channel.sendMessage(author.getAsMention() +  " ``Invalid number``").queue();
				return;
			}
		}

		AudioTrack track = AudioPlayerThread.getMusicManager().player.getPlayingTrack();
		track.setPosition(track.getPosition() + (1000*seconds)); // Lavaplayer handles values < 0 or > track length
	}

}
