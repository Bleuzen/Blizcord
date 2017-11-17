package com.github.bleuzen.blizcord;

public class Values {

	static final boolean DEV = true;

	public static final String BOT_VERSION = "0.8.4" 	+ (DEV ? "-dev" : "");
	public static final String BOT_NAME = "Blizcord";
	public static final String BOT_DEVELOPER = "Bleuzen <supgesu@gmail.com>";
	public static final String BOT_GITHUB_REPO = "Bleuzen/Blizcord";

	public static final String DISCORD_GET_TOKEN = "https://discordapp.com/developers/applications/me";

	public static final String SEARCH_PREFIX_YOUTUBE = "ytsearch:";

	public static final int MAX_MESSAGE_LENGHT = 2000; // Discord's message length limit is 2000

	public static final int EXIT_CODE_RESTART_GUI = 2;

	public static final String OS_LINUX = "linux";
	public static final String OS_WINDOWS = "windows";
	public static final String UNKNOWN_OS = "unknown";

	public static final String CONFIG_FILE_EXTENSION = "json";
	static final String DEFAULT_CONFIG_FILENAME = "config." + CONFIG_FILE_EXTENSION;

	public static final int SET_VOLUME_SUCCESSFULLY = 0;
	public static final int SET_VOLUME_ERROR_CUSTOM_VOLUME_NOT_ALLOWED = 1;
	public static final int SET_VOLUME_ERROR_INVALID_NUMBER = 2;

}
