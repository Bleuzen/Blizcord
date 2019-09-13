package com.github.bleuzen.blizcord.bot.commands;

import com.github.bleuzen.blizcord.bot.AudioPlayerThread;
import com.github.bleuzen.blizcord.bot.Bot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

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

		// Join the voice channel of the command author before playDirect() joins the default channel
		Bot.joinVoiceChannel(Bot.getGuild().getMember(author));

		AudioPlayerThread.playDirect(arg, false);
	}

}
