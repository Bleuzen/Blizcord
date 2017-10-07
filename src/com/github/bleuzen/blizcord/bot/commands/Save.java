package com.github.bleuzen.blizcord.bot.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.commons.io.output.FileWriterWithEncoding;

import com.github.bleuzen.blizcord.Config;
import com.github.bleuzen.blizcord.bot.AudioPlayerThread;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Save extends Command {

	@Override
	public String getName() {
		return "save";
	}

	@Override
	public boolean isAdminOnly() {
		return true;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		// arg = playlist name
		if(arg == null) {
			channel.sendMessage(author.getAsMention() + " ``Please specify a playlist name. Put it behind this command.``").queue();
			return;
		}
		if(!AudioPlayerThread.isPlaying()) {
			channel.sendMessage(author.getAsMention() + " ``The playlist is empty, nothing to save.``").queue();
			return;
		}
		File playlistsFolder = new File(Config.getAppDir(), "playlists");
		if(!playlistsFolder.exists()) {
			if(!playlistsFolder.mkdir()) {
				channel.sendMessage(author.getAsMention() + " ``Failed to create playlists folder.``").queue();
				return;
			}
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(new File(playlistsFolder, arg), StandardCharsets.UTF_8, false));
			// Write currently playing track
			writer.write(AudioPlayerThread.getMusicManager().player.getPlayingTrack().getInfo().uri);
			writer.newLine();
			// Write upcoming tracks
			ArrayList<AudioTrack> upcoming = AudioPlayerThread.getMusicManager().scheduler.getList();
			if(!upcoming.isEmpty()) {
				for(int i = 0; i < upcoming.size(); i++) {
					writer.write(upcoming.get(i).getInfo().uri);
					writer.newLine();
				}
			}
			// Save
			writer.close();

			channel.sendMessage(author.getAsMention() + " Playlist saved: " + arg).queue();
		} catch (Exception e) {
			channel.sendMessage(author.getAsMention() + " ``Failed to save playlist.``").queue();
		}
	}

}
