package com.github.bleuzen.blizcord.bot.commands;

import java.util.ArrayList;

import com.github.bleuzen.blizcord.Utils;
import com.github.bleuzen.blizcord.Values;
import com.github.bleuzen.blizcord.bot.AudioPlayerThread;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

class List extends Command {

	@Override
	public String getName() {
		return "list";
	}

	@Override
	public boolean isAdminOnly() {
		return false;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		if(AudioPlayerThread.isPlaying()) {
			StringBuilder toSend = new StringBuilder(author.getAsMention() + ""
					+ " Currently playing:"
					+ "\n"
					+ "```"
					+ Utils.getTrackName(AudioPlayerThread.getMusicManager().player.getPlayingTrack())
					+ "```"
					+ "\n");
			if(AudioPlayerThread.getMusicManager().scheduler.getList().size() > 0) {
				toSend.append("Upcoming songs:"
						+ "\n"
						+ "```");
				ArrayList<AudioTrack> list = AudioPlayerThread.getMusicManager().scheduler.getList();
				for(int i = 0; i < list.size(); i++) {
					toSend.append("\n" + Utils.getTrackName(list.get(i)));
				}
				toSend.append("```");
			} else {
				toSend.append("``There are no upcoming songs.``");
			}
			if(toSend.length() > Values.MAX_MESSAGE_LENGHT) {
				final String ending = "...```";
				toSend.setLength((Values.MAX_MESSAGE_LENGHT - ending.length()));
				toSend.append(ending);
			}
			channel.sendMessage(toSend.toString()).queue();
		} else {
			// because gets cleared on stop
			// isPlaying() -> else:
			channel.sendMessage(author.getAsMention() + " ``The playlist is empty.``").queue();
		}
	}

}
