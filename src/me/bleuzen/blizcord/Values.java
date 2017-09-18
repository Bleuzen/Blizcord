package me.bleuzen.blizcord;

public class Values {

	static final boolean DEV = false;

	public static final String BOT_VERSION = "0.5.6" 	+ (DEV ? "-dev" : "");
	public static final String BOT_NAME = "Blizcord";
	public static final String BOT_DEVELOPER = "Bleuzen <supgesu@gmail.com>";
	public static final String BOT_GITHUB_REPO = "Bleuzen/Blizcord";

	static final String DISCORD_GET_TOKEN = "https://discordapp.com/developers/applications/me";

	public static final int MAX_MESSAGE_LENGHT = 2000; // Discord's message length limit is 2000

	static final int EXIT_CODE_RESTART_GUI = 2;

}
