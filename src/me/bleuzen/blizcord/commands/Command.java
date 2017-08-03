package me.bleuzen.blizcord.commands;

import java.util.ArrayList;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public abstract class Command {

	public static ArrayList<Command> commands;

	public static void init() {
		commands = new ArrayList<>();

		commands.add(new About());
		commands.add(new Add());
		commands.add(new Help());
		commands.add(new Jump());
		commands.add(new Kill());
		commands.add(new List());
		commands.add(new Load());
		commands.add(new Loop());
		commands.add(new Next());
		commands.add(new Pause());
		commands.add(new Play());
		commands.add(new Repeat());
		commands.add(new Save());
		commands.add(new Search());
		commands.add(new Seek());
		commands.add(new Shuffle());
		commands.add(new Stop());
		commands.add(new Uptime());
	}

	public boolean compare(String cmd) {
		return cmd.equalsIgnoreCase(getName());
	}

	public abstract String getName();

	public abstract void execute(String arg, User author, MessageChannel channel, Guild guild);

}
