package me.bleuzen.blizcord;
import java.io.File;
import java.util.Timer;

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;

import me.bleuzen.blizcord.Utils.ArgumentUtils;
import me.bleuzen.blizcord.commands.Command;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;

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
			updateChecker = new UpdateChecker(Values.BOT_GITHUB_REPO);
		}

		return updateChecker;
	}

	static JDA getApi() {
		return api;
	}

	static Guild getGuild() {
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

	static void launch(String[] args) {
		Log.info("Version: " + Values.BOT_VERSION);
		Log.info("Developer: " + Values.BOT_DEVELOPER);

		Log.info("Starting bot ...");

		// init config
		String configArg = ArgumentUtils.getArg(args, "--config");
		if(configArg != null) {
			configFile = new File(configArg);
		} else {
			configFile = Config.getDefaultConfig();
		}

		Log.info("Config: " + configFile.getAbsolutePath());

		if(!Config.init(configFile, a.isGui())) {
			Utils.errExit("Failed to load config.", Values.EXIT_CODE_RESTART_GUI);
		}

		// Start the bot
		start();
	}

	private static void start() {
		if(Config.get(Config.BOT_TOKEN).isEmpty()) {
			Utils.errExit("You must specify a Token in the config file!", Values.EXIT_CODE_RESTART_GUI);
		}

		Log.info("Starting JDA ...");

		try {
			JDABuilder builder = new JDABuilder(AccountType.BOT).setToken(Config.get(Config.BOT_TOKEN));

			if(Config.getBoolean(Config.AUTO_RECONNECT)) {
				builder.setAutoReconnect(true);
				Log.debug("Auto Reconnect: enabled");
			} else {
				builder.setAutoReconnect(true);
				Log.debug("Auto Reconnect: disabled");
			}

			if(Config.getBoolean(Config.USE_NATIVE_AUDIO_SYSTEM)) {
				builder.setAudioSendFactory(new NativeAudioSendFactory());
				Log.debug("Native audio system: enabled");
			} else {
				Log.debug("Native audio system: disabled");
			}

			api = builder.buildBlocking();

			api.addEventListener(new Bot());


			// test for only one server
			int guilds = api.getGuilds().size();
			if(guilds == 0) {

				// https://discordapi.com/permissions.html#3402768
				String inviteUrl = api.asBot().getInviteUrl(Permission.getPermissions(3402768));

				if(a.isGui()) {
					GUI.addToSever(inviteUrl);
				} else {
					Log.info("To add me to your server visit:" + System.lineSeparator() + inviteUrl);
				}

				// wait until the bot get added to a server
				while(api.getGuilds().size() == 0) {
					Thread.sleep(200);
				}

			} else if(guilds > 1) {
				Utils.errExit("The bot is on more than 1 server. This is currently not supported.", Values.EXIT_CODE_RESTART_GUI);
			}

			guild = api.getGuilds().get(0);

			try {
				GuildController controller = guild.getController();
				if(guild.getTextChannelsByName(Config.get(Config.CONTROL_CHANNEL), true).isEmpty()) { // create channel if not exists
					controller.createTextChannel(Config.get(Config.CONTROL_CHANNEL)).complete();
					Log.info("Created control channel.");
				}
				if(guild.getVoiceChannelsByName(Config.get(Config.VOICE_CHANNEL), true).isEmpty()) {
					controller.createVoiceChannel(Config.get(Config.VOICE_CHANNEL)).complete();
					Log.info("Created music channel.");
				}
			} catch(Exception e) {
				Log.debug("Failed to create channels.");
			}

			try {
				controlChannel = guild.getTextChannelsByName(Config.get(Config.CONTROL_CHANNEL), true).get(0); // true for Ignore Case
			} catch(IndexOutOfBoundsException e) {
				Utils.errExit("There is no '" + Config.get(Config.CONTROL_CHANNEL) + "' Text Channel.", Values.EXIT_CODE_RESTART_GUI);
			}

			String adminsRoleName = Config.get(Config.ADMINS_ROLE);
			if(!adminsRoleName.isEmpty()) {
				try {
					adminRole = guild.getRolesByName(adminsRoleName, true).get(0); // true for Ignore Case
				} catch(IndexOutOfBoundsException e) {
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
		} catch (Exception e) {
			Utils.errExit(e.getMessage(), Values.EXIT_CODE_RESTART_GUI);
		}

		try {
			controlChannel.sendMessage(Values.BOT_NAME + " v" + Values.BOT_VERSION + " started.\nType ``" + Config.get(Config.COMMAND_PREFIX) + "help`` to see all commands.").queue();
		} catch (PermissionException e) {
			sendMessage(guild.getOwner().getUser(), "Please give me the permision to read and write in your control channel: " + controlChannel.getName());
		}
	}

	public static void shutdown() {
		Log.info("Shutting down ...");
		//api.shutdown(); // done by shutdown hook of JDA
		System.exit(0);
	}

	public static void joinVoiceChannel() {
		if (!joined) {
			String cName = Config.get(Config.VOICE_CHANNEL);
			VoiceChannel channel = guild.getVoiceChannels().stream().filter(vChan -> vChan.getName().equalsIgnoreCase(cName)).findFirst().orElse(null);
			try {
				// for multi server: GuildMessageReceivedEvent#event.getGuild()
				guild.getAudioManager().openAudioConnection(channel);
				joined = true;
			} catch(Exception e) {
				controlChannel.sendMessage("Failed to join voice channel: " + cName + "\n"
						+ "Please check your config and give me the permission to join it.").queue();
			}
		}
	}

	static void leaveVoiceChannel() {
		guild.getAudioManager().closeAudioConnection();
		joined = false;
	}


	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String message = event.getMessage().getContent();
		MessageChannel channel = event.getChannel();
		User author = event.getAuthor();

		if ( (channel == controlChannel || channel.getType() == ChannelType.PRIVATE) && message.startsWith(Config.get(Config.COMMAND_PREFIX)) && (!author.getId().equals(api.getSelfUser().getId())) ) {

			Log.debug("Got command from {}: {}", author.getName(), message);

			String[] cmdarg = message.substring(Config.get(Config.COMMAND_PREFIX).length()).split(" ", 2);
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

			if(commandFound) {
				return;
			}

			channel.sendMessage(author.getAsMention() + " ``Unknown command``").queue();

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

	static void setGame(Game game) {
		api.getPresence().setGame(game);

		Log.debug("Set game to: {}", game);
	}

	public static void sendUpdateMessage(boolean toOwner) {
		// Check if bot is already running (could not when starting GUI)
		// prevent NullPointerException
		if(api != null) {
			String uMsg = "A new version is available!\n"
					+ "https://github.com/" + Values.BOT_GITHUB_REPO + "/releases";

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
				new Timer().schedule(getUpdateChecker(), a.isGui() ? hours : 5000, hours);
			}
		}
	}

}

