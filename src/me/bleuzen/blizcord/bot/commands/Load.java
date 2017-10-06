package me.bleuzen.blizcord.bot.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import me.bleuzen.blizcord.Config;
import me.bleuzen.blizcord.bot.AudioPlayerThread;
import me.bleuzen.blizcord.bot.Bot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Load extends Command {

	@Override
	public String getName() {
		return "load";
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
		// Load the playlist
		try {
			File playlistFile = new File(new File(Config.getAppDir(), "playlists"), arg);
			if(!playlistFile.exists()) {
				channel.sendMessage(author.getAsMention() + " Playlist doesn't exist: " + arg).queue();
				return;
			}

			Bot.joinVoiceChannel(); // try to join if not already

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(playlistFile), StandardCharsets.UTF_8));
			String line;
			while((line = bufferedReader.readLine()) != null) {
				AudioPlayerThread.loadAndPlay(channel, line, false, true);
			}
			bufferedReader.close();

			channel.sendMessage(author.getAsMention() + " Playlist loaded: " + arg).queue();
		} catch (Exception e) {
			channel.sendMessage(author.getAsMention() + " Failed to load playlist: " + arg).queue();
		}
	}

}
