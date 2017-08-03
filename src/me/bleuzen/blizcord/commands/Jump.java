package me.bleuzen.blizcord.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import me.bleuzen.blizcord.PlayerThread;
import me.bleuzen.blizcord.Utils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Jump extends Command {

	@Override
	public String getName() {
		return "jump";
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		if(!Utils.isAdmin(author)) {
			channel.sendMessage(author.getAsMention() + " ``Only admins can jump.``").queue();
			return;
		}

		if(!PlayerThread.isPlaying()) {
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
				channel.sendMessage(author.getAsMention() +  " Invalid number").queue();
				return;
			}
		}

		AudioTrack track = PlayerThread.getMusicManager().player.getPlayingTrack();
		track.setPosition(track.getPosition() + (1000*seconds)); // Lavaplayer handles values < 0 or > track length
	}

}
