package com.github.bleuzen.blizcord.bot.commands;

import java.util.ArrayList;

import com.github.bleuzen.blizcord.Config;
import com.github.bleuzen.blizcord.Utils;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public abstract class Command {

	public static ArrayList<Command> commands;

	private static String prefix;

	public static void init() {
		prefix = Config.get(Config.COMMAND_PREFIX);

		commands = new ArrayList<>();

		commands.add(new About());
		commands.add(new Add());
		commands.add(new Help());
		commands.add(new Jump());
		commands.add(new Kill());
		commands.add(new List());
		commands.add(new Lists());
		commands.add(new Load());
		commands.add(new Loadshuffle());
		commands.add(new Loop());
		commands.add(new Next());
		commands.add(new Pause());
		commands.add(new Play());
		commands.add(new Playtime());
		commands.add(new Repeat());
		commands.add(new Save());
		commands.add(new Search());
		commands.add(new Seek());
		commands.add(new Shuffle());
		commands.add(new Stop());
		commands.add(new Uptime());
		commands.add(new Volume());
	}

	public static String getPrefix() {
		return prefix;
	}

	public boolean compare(String cmd) {
		return cmd.equalsIgnoreCase(getName());
	}

	public boolean hasPermission(User user) {
		if(isAdminOnly()) {
			return Utils.isAdmin(user);
		} else {
			return true;
		}
	}

	public abstract String getName();

	public abstract boolean isAdminOnly();

	public abstract void execute(String arg, User author, MessageChannel channel, Guild guild);

}
