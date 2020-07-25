package com.github.bleuzen.blizcord.bot.commands;

import com.github.bleuzen.blizcord.Config;
import com.github.bleuzen.blizcord.bot.AudioPlayerThread;
import com.github.bleuzen.blizcord.bot.Bot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

class Rmlist extends Command {

	@Override
	public String getName() {
		return "rmlist";
	}

	@Override
	public boolean isAdminOnly() {
		return true;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		// arg = playlist
		if(arg == null) {
			channel.sendMessage(author.getAsMention() + " ``Please specify a playlist name. Put it behind this command.``").queue();
			return;
		}
		File playlistFile = new File(new File(Config.getAppDir(), "playlists"), arg);
		if(!playlistFile.exists()) {
			channel.sendMessage(author.getAsMention() + " Playlist doesn't exist: ``" + arg + "``").queue();
			return;
		}
		try {
			playlistFile.delete();
			channel.sendMessage(author.getAsMention() + " Playlist deleted: ``" + arg + "``").queue();
		} catch (Exception e) {
			channel.sendMessage(author.getAsMention() + " Failed to delete playlist: ``" + arg + "``").queue();
		}
	}

}
