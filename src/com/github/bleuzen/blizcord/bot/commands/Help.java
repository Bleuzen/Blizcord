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
				+ getPrefix() + "list                           (Show the playlist)\n"
				+ getPrefix() + "play <file or link>            (Play given track now)\n"
				+ getPrefix() + "add <file, folder or link>     (Add given track to playlist)\n"
				+ getPrefix() + "search <youtube video title>   (Plays the first video that was found)\n"
				+ getPrefix() + "save <playlist>                (Save the current playlist)\n"
				+ getPrefix() + "load <playlist>                (Load a saved playlist)\n"
				+ getPrefix() + "loadshuffle <playlist>         (Load a list and shuffle it)\n"
				+ getPrefix() + "rmlist <playlist>              (Delete a saved playlist)\n"
				+ getPrefix() + "lists                          (List all saved playlists)\n"
				+ getPrefix() + "pause                          (Pause or resume the current track)\n"
				+ getPrefix() + "stop                           (Stop the playback and clear the playlist)\n"
				+ getPrefix() + "volume                         (Change the playback volume)\n"
				+ getPrefix() + "next (<how many songs>)        (Skip one or more songs from the playlist)\n"
				+ getPrefix() + "seek <hours:minutes:seconds>   (Seek to the specified position)\n"
				+ getPrefix() + "jump (<how many seconds>)      (Jump forward in the current track)\n"
				+ getPrefix() + "repeat (<how many times>)      (Repeat the current playlist)\n"
				+ getPrefix() + "shuffle                        (Randomize the track order)\n"
				+ getPrefix() + "loop                           (Re add played track to the end of the playlist)\n"
				+ getPrefix() + "playtime                       (Time in currently playing song)\n"
				+ getPrefix() + "uptime                         (See how long the bot is already online)\n"
				+ getPrefix() + "about                          (Print about message)\n"
				+ getPrefix() + "kill                           (Kill the bot)"
				+ "```"
				+ (channel.getType() == ChannelType.PRIVATE ? ("\n**Guild:** " + guild.getName()) : "") ).queue();
	}

}
