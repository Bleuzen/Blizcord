package com.github.bleuzen.blizcord.bot;

import java.io.File;
import java.util.Timer;
import com.github.bleuzen.blizcord.Config;
import com.github.bleuzen.blizcord.Log;
import com.github.bleuzen.blizcord.NativeKeyListener;
import com.github.bleuzen.blizcord.UpdateChecker;
import com.github.bleuzen.blizcord.Utils;
import com.github.bleuzen.blizcord.Utils.ArgumentUtils;
import com.github.bleuzen.blizcord.Values;
import com.github.bleuzen.blizcord.a;
import com.github.bleuzen.blizcord.bot.commands.Command;
import com.github.bleuzen.blizcord.gui.GUI_Main;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Bot extends ListenerAdapter {

	private static File configFile;

	private static JDA api;
	private static Guild guild;
	private static Role adminRole;
	private static TextChannel controlChannel;

	private static long startTime;

	public static boolean joined;

	private static UpdateChecker updateChecker;

	public static UpdateChecker getUpdateChecker() {
		if(updateChecker == null) {

			// Get sure that it doesn't work if disabled
			if(a.isDisableUpdateChecker()) {
				return null;
			}

			// Init updateChecker
			updateChecker = new UpdateChecker();
		}

		return updateChecker;
	}

	static JDA getApi() {
		return api;
	}

	public static Guild getGuild() {
		return guild;
	}

	public static Role getAdminRole() {
		return adminRole;
	}

	static TextChannel getControlChannel() {
		return controlChannel;
	}

	public static long getStartTime() {
		return startTime;
	}

	public static boolean launch(String[] args) {
		Log.info("Starting bot...");

		// init config
		String configArg = ArgumentUtils.getArg(args, "--config");
		if(configArg != null) {
			configFile = new File(configArg);
		} else {
			configFile = Config.getDefaultConfig();
		}

		Log.info("Config: " + configFile.getAbsolutePath());

		// Load the config (if not already done (by GUI_Config))
		if(!Config.isInitialized()) {
			if(!Config.init(configFile, a.isGui())) {
				Utils.errExit("Failed to load config.", Values.EXIT_CODE_RESTART_GUI);
			}
		}

		// Start the bot
		return start();
	}

	private static boolean start() {
		if(Config.get(Config.BOT_TOKEN).isEmpty()) {
			Utils.handleErrorNCG("You must specify a Token in the config file!");
			return false;
		}

		Log.info("Starting JDA...");

		try {
			JDABuilder builder = JDABuilder.createDefault(Config.get(Config.BOT_TOKEN));

			builder.addEventListeners(new Bot());

			if(Config.getBoolean(Config.AUTO_RECONNECT)) {
				builder.setAutoReconnect(true);
				Log.debug("Auto Reconnect: enabled");
			} else {
				builder.setAutoReconnect(true);
				Log.debug("Auto Reconnect: disabled");
			}

			if(Config.getBoolean(Config.USE_NATIVE_AUDIO_SYSTEM)) {
				builder.setAudioSendFactory(new NativeAudioSendFactory());
				//builder.setAudioSendFactory(new NativeAudioSendFactory(bufferDuration));
				Log.debug("Native audio system: enabled");
			} else {
				Log.debug("Native audio system: disabled");
			}

			// Connect to Discord
			api = builder.build();
			// Wait until connected
			api.awaitReady();

			// check if only one server
			int guilds = api.getGuilds().size();
			if(guilds == 0) {

				// https://discordapi.com/permissions.html#3402768
				String inviteUrl = api.getInviteUrl(Permission.getPermissions(3402768));

				if(a.isGui()) {
					GUI_Main.addToSever(inviteUrl);
				} else {
					Log.info("To add me to your server visit:" + System.lineSeparator() + inviteUrl);
				}

				// wait until the bot got added to a server
				while(api.getGuilds().size() == 0) {
					Thread.sleep(200);
				}

			} else if(guilds > 1) {
				Utils.errExit("The bot is on more than 1 server. This is currently not supported.", Values.EXIT_CODE_RESTART_GUI);
			}

			guild = api.getGuilds().get(0);

			try {
				if(guild.getTextChannelsByName(Config.get(Config.CONTROL_CHANNEL), true).isEmpty()) { // create channel if not exists
					guild.createTextChannel(Config.get(Config.CONTROL_CHANNEL)).complete();
					Log.info("Created control channel.");
				}
				if(guild.getVoiceChannelsByName(Config.get(Config.VOICE_CHANNEL), true).isEmpty()) {
					guild.createVoiceChannel(Config.get(Config.VOICE_CHANNEL)).complete();
					Log.info("Created default music channel.");
				}
			} catch(Exception e) {
				Utils.printException(e);
				Log.debug("Failed to create channels.");
			}

			try {
				controlChannel = guild.getTextChannelsByName(Config.get(Config.CONTROL_CHANNEL), true).get(0); // true for Ignore Case
			} catch(IndexOutOfBoundsException e) {
				Utils.printException(e);
				//TODO: Does it really need to exit here?
				Utils.errExit("There is no '" + Config.get(Config.CONTROL_CHANNEL) + "' Text Channel.", Values.EXIT_CODE_RESTART_GUI);
			}

			String adminsRoleName = Config.get(Config.ADMINS_ROLE);
			if(!adminsRoleName.isEmpty()) {
				try {
					adminRole = guild.getRolesByName(adminsRoleName, true).get(0); // true for Ignore Case
				} catch(IndexOutOfBoundsException e) {
					Utils.printException(e);
					//TODO: Does it really need to exit here?
					Utils.errExit("There is no '" + adminsRoleName + "' Role.", Values.EXIT_CODE_RESTART_GUI);
				}
			}

			// Init Player
			AudioPlayerThread.init();

			// Start game update thread
			if(Config.getBoolean(Config.DISPLAY_SONG_AS_GAME)) {
				new Thread(new AudioPlayerThread()).start();
			}

			// Start NativeKeyListener
			if(Config.getBoolean(Config.ENABLE_MEDIA_CONTROL_KEYS)) {
				NativeKeyListener.init();
			}

			// Start checking for updates
			startUpdateChecker();

			// Init commands
			Command.init();

			// Save start time
			startTime = System.currentTimeMillis();

			Log.info("Successfully started.");

			try {
				controlChannel.sendMessage(Values.BOT_NAME + " v" + Values.BOT_VERSION + " started.\nType ``" + Command.getPrefix() + "help`` to see all commands.").queue();
			} catch (PermissionException e) {
				sendMessage(guild.getOwner().getUser(), "Please give me the permision to read and write in your control channel: " + controlChannel.getName());
			}

			return true;

		} catch (Exception e) {
			Utils.handleExceptionNCG(e);
			return false;
		}

	}

	public static void shutdown() {
		Log.info("Shutting down ...");

		// Save volume
		if(Config.getBoolean(Config.ALLOW_CUSTOM_VOLUME)) {
			Config.set(Config.STARTING_VOLUME, AudioPlayerThread.getVolume());
			Config.save();
		}

		// Shutdown JDA
		//api.shutdown(); // done by shutdown hook of JDA
		System.exit(0);
	}

	public static void joinVoiceChannel() {
		if (joined) {
			return;
		}

		String channelName = Config.get(Config.VOICE_CHANNEL);
		VoiceChannel channel = guild.getVoiceChannels().stream().filter(vChan -> vChan.getName().equalsIgnoreCase(channelName)).findFirst().orElse(null);

		if(channel == null) {
			controlChannel.sendMessage("Voice channel not found: " + channelName).queue();
			return;
		}

		joinVoiceChannel(channel);
	}

	public static void joinVoiceChannel(Member member) {
		if (joined) {
			return;
		}

		if(member == null) {
			joinVoiceChannel(); // join default channel
			return;
		}

		VoiceChannel channel = member.getVoiceState().getChannel();

		// Check if member is not in a channel
		if(channel == null) {
			joinVoiceChannel(); // join default channel
			return;
		}

		joinVoiceChannel(channel);
	}

	public static void joinVoiceChannel(VoiceChannel voiceChannel) {
		if (joined) {
			return;
		}

		if(voiceChannel == null) {
			return;
		}

		try {
			guild.getAudioManager().openAudioConnection(voiceChannel);
			joined = true;
		} catch(Exception e) {
			Utils.printException(e);
			controlChannel.sendMessage("Failed to join voice channel: " + voiceChannel + "\n"
					+ "Do I have the permission to join it?").queue();
		}
	}

	static void leaveVoiceChannel() {
		guild.getAudioManager().closeAudioConnection();
		joined = false;
	}


	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String message = event.getMessage().getContentDisplay();
		MessageChannel channel = event.getChannel();
		User author = event.getAuthor();

		if ( (channel == controlChannel || channel.getType() == ChannelType.PRIVATE) && message.startsWith(Command.getPrefix()) && (!author.getId().equals(api.getSelfUser().getId())) ) {

			Log.debug("Got command from {}: {}", author.getName(), message);

			String[] cmdarg = message.substring(Command.getPrefix().length()).split(" ", 2);
			String cmd = cmdarg[0];
			String arg;
			try {
				arg = cmdarg[1];
			} catch (IndexOutOfBoundsException e) {
				arg = null;
			}

			boolean commandFound = false;

			for(Command command : Command.commands) {
				if (command.compare(cmd)) {
					commandFound = true;

					if(command.hasPermission(author)) {
						command.execute(arg, author, channel, guild);
					} else {
						channel.sendMessage(author.getAsMention() + " ``You are not allowed to use this command.``").queue();
					}

					break;
				}
			}

			if(!commandFound) {
				channel.sendMessage(author.getAsMention() + " ``Unknown command``").queue();
			}
		}
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		Utils.errExit("I got kicked.");
	}

	public static void stopPlayer() {
		// stop the music
		AudioPlayerThread.stop();
		// leave the channel
		leaveVoiceChannel();
		// cancel skipping
		AudioPlayerThread.skipping = false;
		// clear the playlist
		AudioPlayerThread.getMusicManager().scheduler.clear();
		// reset pause state
		AudioPlayerThread.setPaused(false);
	}

	static void setActivity(Activity activity) {
		api.getPresence().setActivity(activity);

		Log.debug("Set Activity to: {}", activity);
	}

	public static void sendUpdateMessage(boolean toOwner) {
		// Check if bot is already running (could not when starting GUI)
		// prevent NullPointerException
		if(api != null) {
			String uMsg = "A new version is available!\n"
					+ getUpdateChecker().getDownloadUrl();

			if(toOwner) {
				sendMessage(guild.getOwner().getUser(), uMsg);
			} else {
				controlChannel.sendMessage(uMsg).queue();
			}
		}
	}

	private static void sendMessage(User user, String msg) {
		user.openPrivateChannel().queue(privateChannel -> {
			privateChannel.sendMessage(msg).queue();
		});
	}

	private static void startUpdateChecker() {
		if(!a.isDisableUpdateChecker()) {
			int updateCheckInterval;
			try {
				updateCheckInterval = Integer.parseInt(Config.get(Config.UPDATE_CHECK_INTERVAL_HOURS));
			} catch (NumberFormatException e) {
				updateCheckInterval = 0;
			}
			if (updateCheckInterval > 0) {
				int hours = (1000 * 3600 * updateCheckInterval);
				// First update check only 5 seconds delayed if NOT from GUI (send message on Discord)
				// If from GUI, check after some hours (again)
				// This has nothing to do with the first update check after GUI start
				new Timer().schedule(getUpdateChecker(), a.isGui() ? hours : 5000, hours);
			}
		}
	}

}

