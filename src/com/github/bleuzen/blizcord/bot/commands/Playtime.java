package com.github.bleuzen.blizcord.bot.commands;

import com.github.bleuzen.blizcord.Utils;
import com.github.bleuzen.blizcord.bot.AudioPlayerThread;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class Playtime extends Command {

	@Override
	public String getName() {
		return "playtime";
	}

	@Override
	public boolean isAdminOnly() {
		return false;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		if(!AudioPlayerThread.isPlaying()) {
			channel.sendMessage(author.getAsMention() + " No track is playing.").queue();
			return;
		}

		AudioTrack track = AudioPlayerThread.getCurrentTrack();
		long dur = track.getDuration();
		long pos = track.getPosition();

		channel.sendMessage(author.getAsMention() + " ``" + Utils.durationToTackPosition(pos) + " / " + Utils.durationToTackPosition(dur) + "``").queue();
	}

}
