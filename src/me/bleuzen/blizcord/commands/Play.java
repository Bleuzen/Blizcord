package me.bleuzen.blizcord.commands;

import me.bleuzen.blizcord.AudioPlayerThread;
import me.bleuzen.blizcord.Bot;
import me.bleuzen.blizcord.Utils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Play extends Command {

	@Override
	public String getName() {
		return "play";
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
		if(!Utils.isAdmin(author)) {
			channel.sendMessage(author.getAsMention() + " ``Sorry, only admins can play something direct.``").queue();
			return;
		}

		if(arg == null) {
			channel.sendMessage(author.getAsMention() + " ``Please specify what I should play. Put it behind this command.``").queue();
			return;
		}

		Bot.joinVoiceChannel(); // try to join if not already

		if(Bot.joined) { // if successfully joined
			AudioPlayerThread.loadAndPlay(channel, arg, true, false);
		}
	}

}
