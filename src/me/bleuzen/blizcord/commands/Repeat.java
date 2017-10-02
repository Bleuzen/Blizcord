package me.bleuzen.blizcord.commands;

import java.util.ArrayList;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import me.bleuzen.blizcord.AudioPlayerThread;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Repeat extends Command {

	@Override
	public String getName() {
		return "repeat";
	}

	@Override
	public boolean isAdminOnly() {
		return true;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		int repeats;
		if(arg == null) {
			repeats = 1;
		} else {
			try {
				repeats = Integer.parseInt(arg);
				if(repeats < 1) {
					throw new NumberFormatException();
				}
			} catch(NumberFormatException e) {
				channel.sendMessage(author.getAsMention() + " Invalid number").queue();
				return;
			}
		}

		if(AudioPlayerThread.isPlaying()) {

			ArrayList<AudioTrack> songs = new ArrayList<>();
			songs.add(AudioPlayerThread.getMusicManager().player.getPlayingTrack());
			ArrayList<AudioTrack> upcoming = AudioPlayerThread.getMusicManager().scheduler.getList();
			if(!upcoming.isEmpty()) {
				for(int i = 0; i < upcoming.size(); i++) {
					songs.add(upcoming.get(i));
				}
			}

			for(int i = 0; i < repeats; i++) {
				for(int j = 0; j < songs.size(); j++) {
					AudioPlayerThread.play(songs.get(j).makeClone());
				}
			}

			channel.sendMessage( "``Repeated the playlist" + (repeats == 1 ? ".``" : (" " + repeats + " times.``") )).queue();
		} else {
			channel.sendMessage(author.getAsMention() + " ``The playlist is empty. There is nothing to repeat.``").queue();
		}
	}

}
