package me.bleuzen.blizcord;
import java.io.File;
import java.util.Arrays;
import java.util.Timer;

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

	private static JDA api;
	private static Guild guild;
	private static Role adminRole;
	private static TextChannel controlChannel;

	private static long startTime;

	public static UpdateChecker updateChecker;

	public static boolean joined;

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

	public static void start() {
		if(Config.get(Config.BOT_TOKEN).isEmpty()) {
			a.errExit("You must specify a Token in the config file!", Values.EXIT_CODE_RESTART_GUI);
		}

		Log.info("Starting JDA ...");

		try {
			api = new JDABuilder(AccountType.BOT).setToken(Config.get(Config.BOT_TOKEN))
					//.setEnableShutdownHook(false) // default: true
					.buildBlocking();
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
				a.errExit("The bot is on more than 1 server. This is currently not supported.", Values.EXIT_CODE_RESTART_GUI);
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
				a.errExit("There is no '" + Config.get(Config.CONTROL_CHANNEL) + "' Text Channel.", Values.EXIT_CODE_RESTART_GUI);
			}

			String adminsRoleName = Config.get(Config.ADMINS_ROLE);
			if(!adminsRoleName.isEmpty()) {
				try {
					adminRole = guild.getRolesByName(adminsRoleName, true).get(0); // true for Ignore Case
				} catch(IndexOutOfBoundsException e) {
					a.errExit("There is no '" + adminsRoleName + "' Role.", Values.EXIT_CODE_RESTART_GUI);
				}
			}

			// Init Player
			PlayerThread.init();

			// Start game update thread
			if(Boolean.valueOf(Config.get(Config.DISPLAY_SONG_AS_GAME))) {
				new Thread(new PlayerThread()).start();
			}

			// Start NativeKeyListener
			if(Boolean.valueOf(Config.get(Config.ENABLE_MEDIA_CONTROL_KEYS))) {
				NativeKeyListener.init();
			}

			// Start checking for updates
			int updateCheckInterval;
			try {
				updateCheckInterval= Integer.parseInt(Config.get(Config.UPDATE_CHECK_INTERVAL_HOURS));
			} catch (NumberFormatException e) {
				updateCheckInterval = 0;
			}
			if(updateCheckInterval > 0) {
				// First update check delayed 5 seconds, then all updateCheckInterval hours
				updateChecker = new UpdateChecker();
				new Timer().schedule(updateChecker, 5000, (1000 * 3600 * updateCheckInterval));
			}

			// Init commands
			Command.init();

			// Save start time
			startTime = System.currentTimeMillis();

			Log.info("Successfully started.");
		} catch (Exception e) {
			a.errExit(e.getMessage(), Values.EXIT_CODE_RESTART_GUI);
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

	public static void join() {
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

	static void leave() {
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

			boolean success = false;

			for(Command command : Command.commands) {
				if (command.compare(cmd)) {
					command.execute(arg, author, channel, guild);
					success = true;
					break;
				}
			}

			if (!success) {
				channel.sendMessage(author.getAsMention() + " ``Unknown command``").queue();
			}

		}
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		a.errExit("I got kicked.");
	}

	public static void addToPlaylist(String arg) {
		join(); // try to join if not already

		if(joined) { // if successfully joined

			File inputFile = new File(arg);

			if(inputFile.isDirectory()) {
				controlChannel.sendMessage("Adding all supported files from folder to queue ...").queue();;
				File[] files = inputFile.listFiles();
				Arrays.sort(files);
				int addesFiles = 0;
				for(File f : files) {
					if(f.isFile()) {
						PlayerThread.loadAndPlay(controlChannel, f.getAbsolutePath(), false, true);
						addesFiles++;
					}
				}
				controlChannel.sendMessage("``Added " + addesFiles + " files.``").queue();
			} else {
				PlayerThread.loadAndPlay(controlChannel, arg, false, false);
			}

		}
	}

	public static void stopPlayer() {
		// stop the music
		PlayerThread.stop();
		// leave the channel
		leave();
		// cancel skipping
		PlayerThread.skipping = false;
		// clear the playlist
		PlayerThread.getMusicManager().scheduler.clear();
	}

	static void setGame(Game game) {
		api.getPresence().setGame(game);

		Log.debug("Set game to: {}", game);
	}

	public static void sendUpdateMessage(boolean toOwner) {
		String uMsg = "A new version is available!\n"
				+ "https://github.com/" + Values.BOT_GITHUB_REPO + "/releases";

		if(toOwner) {
			sendMessage(guild.getOwner().getUser(), uMsg);
		} else {
			controlChannel.sendMessage(uMsg).queue();
		}
	}

	private static void sendMessage(User user, String msg) {
		user.openPrivateChannel().queue(privateChannel -> {
			privateChannel.sendMessage(msg).queue();
		});
	}

}

