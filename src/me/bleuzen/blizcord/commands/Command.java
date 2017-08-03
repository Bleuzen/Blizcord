package me.bleuzen.blizcord.commands;

import java.util.ArrayList;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public abstract class Command {

	public static ArrayList<Command> commands;

	public static void init() {
		commands = new ArrayList<>();

		commands.add(new Help());
		commands.add(new Kill());
		commands.add(new Next());
		commands.add(new Seek());
	}

	public boolean compare(String cmd) {
		return cmd.equalsIgnoreCase(getName());
	}

	public abstract String getName();

	public abstract void execute(String arg, User author, MessageChannel channel, Guild guild);

}
