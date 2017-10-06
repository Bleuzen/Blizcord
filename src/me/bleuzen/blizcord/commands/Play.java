package me.bleuzen.blizcord.commands;

import me.bleuzen.blizcord.bot.AudioPlayerThread;
import me.bleuzen.blizcord.bot.Bot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

class Play extends Command {

	@Override
	public String getName() {
		return "play";
	}

	@Override
	public boolean isAdminOnly() {
		return true;
	}

	@Override
	public void execute(String arg, User author, MessageChannel channel, Guild guild) {
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
