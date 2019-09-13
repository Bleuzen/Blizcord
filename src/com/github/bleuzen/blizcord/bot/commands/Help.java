package com.github.bleuzen.blizcord.bot.commands;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

class Help extends Command {

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public boolean isAdminOnly() {
		return false;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		channel.sendMessage(author.getAsMention() + " **Commands:**\n"
				+ "```"
				+ "!list                           (Show the playlist)\n"
				+ "!play <file or link>            (Play given track now)\n"
				+ "!add <file, folder or link>     (Add given track to playlist)\n"
				+ "!search <youtube video title>   (Plays the first video that was found)\n"
				+ "!save <playlist>                (Save the current playlist)\n"
				+ "!load <playlist>                (Load a saved playlist)\n"
				+ "!loadshuffle <playlist>         (Load a list and shuffle it)\n"
				+ "!lists                          (List the saved playlists)\n"
				+ "!pause                          (Pause or resume the current track)\n"
				+ "!stop                           (Stop the playback and clear the playlist)\n"
				+ "!volume                         (Change the playback volume)\n"
				+ "!next (<how many songs>)        (Skip one or more songs from the playlist)\n"
				+ "!seek <hours:minutes:seconds>   (Seek to the specified position)\n"
				+ "!jump (<how many seconds>)      (Jump forward in the current track)\n"
				+ "!repeat (<how many times>)      (Repeat the current playlist)\n"
				+ "!shuffle                        (Randomize the track order)\n"
				+ "!loop                           (Re add played track to the end of the playlist)\n"
				+ "!playtime                       (Time in currently playing song)\n"
				+ "!uptime                         (See how long the bot is already online)\n"
				+ "!about                          (Print about message)\n"
				+ "!kill                           (Kill the bot)"
				+ "```"
				+ (channel.getType() == ChannelType.PRIVATE ? ("\n**Guild:** " + guild.getName()) : "") ).queue();
	}

}
