import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.output.FileWriterWithEncoding;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.JDAInfo;
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

	private static UpdateChecker updateChecker;

	static boolean joined;

	static JDA getApi() {
		return api;
	}

	static Guild getGuild() {
		return guild;
	}

	static TextChannel getControlChannel() {
		return controlChannel;
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

	static void shutdown() {
		Log.info("Shutting down ...");
		//api.shutdown(); // done by shutdown hook of JDA
		System.exit(0);
	}

	private static void join() {
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

	private static boolean isAdmin(User user) {
		return user.getId().equals(guild.getOwner().getUser().getId()) || (adminRole != null && guild.getMember(user).getRoles().contains(adminRole));
	}


	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		String message = event.getMessage().getContent();
		MessageChannel channel = event.getChannel();
		User author = event.getAuthor();

		if ( (channel == controlChannel || channel.getType() == ChannelType.PRIVATE) && message.startsWith(Config.get(Config.COMMAND_PREFIX)) && (!author.getId().equals(api.getSelfUser().getId())) ) {

			Log.debug("Got command from {}: {}", author.getName(), message);

			String[] cmdarg = message.substring(Config.get(Config.COMMAND_PREFIX).length()).split(" ", 2);
			String cmd = cmdarg[0].toLowerCase();
			String arg;
			try {
				arg = cmdarg[1];
			} catch (IndexOutOfBoundsException e) {
				arg = null;
			}

			switch (cmd) {
			case "help":
				channel.sendMessage(author.getAsMention() + " **Commands:**\n"
						+ "```"
						+ "!list                           (Show the playlist)\n"
						+ "!play <file or link>            (Play given track now)\n"
						+ "!add <file, folder or link>     (Add given track to playlist)\n"
						+ "!search <youtube video title>   (Plays the first video that was found)\n"
						+ "!save <name>                    (Save the current playlist)\n"
						+ "!load <name>                    (Load a saved playlist)\n"
						+ "!pause                          (Pause or resume the current track)\n"
						+ "!skip (<how many songs>)        (Skip one or more songs from the playlist)\n"
						+ "!seek <hours:minutes:seconds>   (Seek to the specified position)\n"
						+ "!jump (<how many seconds>)      (Jump forward in the current track)\n"
						+ "!repeat (<how many times>)      (Repeat the current playlist)\n"
						+ "!shuffle                        (Randomize the track order)\n"
						+ "!loop                           (Re add played track to the end of the playlist)\n"
						+ "!stop                           (Stop the playback and clear the playlist)\n"
						+ "!uptime                         (See how long the bot is already online)\n"
						+ "!about                          (Print about message)\n"
						+ "!kill                           (Kill the bot)"
						+ "```"
						+ (channel.getType() == ChannelType.PRIVATE ? ("\n**Guild:** " + guild.getName()) : "") ).queue();

				break;


			case "kill":
				if(isAdmin(author)) {
					channel.sendMessage("Bye").complete(); // complete(): block this thread (send the message first, than shutdown)
					shutdown();
				} else {
					channel.sendMessage(author.getAsMention() + " ``Only admins can kill me.``").queue();
				}

				break;


			case "skip":
				if(!isAdmin(author)) {
					channel.sendMessage(author.getAsMention() + " ``Only admins can skip.``").queue();
					return;
				}

				if(!PlayerThread.isPlaying()) {
					channel.sendMessage(author.getAsMention() + " ``Currently I'm not playing.``").queue();
					return;
				}

				int skips;
				if (arg == null) {
					skips = 1;
				} else {
					try {
						skips = Integer.parseInt(arg);
						if (skips < 1) {
							throw new NumberFormatException();
						}
					} catch (NumberFormatException e) {
						channel.sendMessage(author.getAsMention() + " Invalid number").queue();
						return;
					}
				}

				PlayerThread.getMusicManager().scheduler.nextTrack(skips);

				break;


			case "seek":
				if(!isAdmin(author)) {
					channel.sendMessage(author.getAsMention() + " ``Only admins can use this command.``").queue();
					return;
				}

				if(!PlayerThread.isPlaying()) {
					channel.sendMessage(author.getAsMention() + " ``Currently I'm not playing.``").queue();
					return;
				}

				if(arg == null) {
					channel.sendMessage(author.getAsMention() + " ``Please specify a time. Put it behind this command. Split hours, minutes and seconds with ':'. Hours and minutes are optional.``").queue();
					return;
				}

				long ms = -1; // invalid by default
				try {
					int c = arg.length() - arg.replace(":", "").length();
					if(c == 2) {
						// hours, minutes and seconds
						String[] split = arg.split(":");
						ms = timeToMS(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
					} else if(c == 1) {
						// minutes and seconds
						String[] split = arg.split(":");
						ms = timeToMS(0, Integer.parseInt(split[0]), Integer.parseInt(split[1]));
					} else if(c == 0) {
						// only seconds
						ms = timeToMS(0, 0, Integer.parseInt(arg));
					}

					if(ms < 0) {
						throw new NumberFormatException();
					}
				} catch(Exception e) {
					channel.sendMessage(author.getAsMention() +  " Invalid time").queue();
					return;
				}

				PlayerThread.getMusicManager().player.getPlayingTrack().setPosition(ms);

				break;


			case "jump":
				if(!isAdmin(author)) {
					channel.sendMessage(author.getAsMention() + " ``Only admins can jump.``").queue();
					return;
				}

				if(!PlayerThread.isPlaying()) {
					channel.sendMessage(author.getAsMention() + " ``Currently I'm not playing.``").queue();
					return;
				}

				int seconds;
				if(arg == null) {
					seconds = 10;
				} else {
					try {
						seconds = Integer.parseInt(arg);
						if(seconds == 0) {
							throw new NumberFormatException();
						}
					} catch(NumberFormatException e) {
						channel.sendMessage(author.getAsMention() +  " Invalid number").queue();
						return;
					}
				}

				AudioTrack track = PlayerThread.getMusicManager().player.getPlayingTrack();
				track.setPosition(track.getPosition() + (1000*seconds)); // Lavaplayer handles values < 0 or > track length

				break;


			case "repeat":
				if(!isAdmin(author)) {
					channel.sendMessage(author.getAsMention() + " ``Sorry, only admins can use the repeat command.``").queue();
					return;
				}

				int repeats;
				if(arg == null) {
					repeats = 1;
				} else {
					try {
						repeats = Integer.parseInt(arg);
						if(repeats < 1) {
							throw new NumberFormatException();
						}
					} catch(NumberFormatException e) {
						channel.sendMessage(author.getAsMention() + " Invalid number").queue();
						return;
					}
				}

				if(PlayerThread.isPlaying()) {

					ArrayList<AudioTrack> songs = new ArrayList<>();
					songs.add(PlayerThread.getMusicManager().player.getPlayingTrack());
					ArrayList<AudioTrack> upcoming = PlayerThread.getMusicManager().scheduler.getList();
					if(!upcoming.isEmpty()) {
						for(int i = 0; i < upcoming.size(); i++) {
							songs.add(upcoming.get(i));
						}
					}

					for(int i = 0; i < repeats; i++) {
						for(int j = 0; j < songs.size(); j++) {
							PlayerThread.play(songs.get(j).makeClone());
						}
					}

					channel.sendMessage( "``Repeated the playlist" + (repeats == 1 ? ".``" : (" " + repeats + " times.``") )).queue();
				} else {
					channel.sendMessage(author.getAsMention() + " ``The playlist is empty. There is nothing to repeat.``").queue();
				}

				break;


			case "shuffle":
				if(!isAdmin(author)) {
					channel.sendMessage(author.getAsMention() + " ``Only admins can shuffle the playlist.``").queue();
					return;
				}

				PlayerThread.getMusicManager().scheduler.shuffle();

				channel.sendMessage(author.getAsMention() + " ``The playlist got shuffeled.``").queue();

				break;


			case "loop":
				if(!isAdmin(author)) {
					channel.sendMessage(author.getAsMention() + " ``Only admins can enable loop.``").queue();
					return;
				}

				if(PlayerThread.loop) {
					PlayerThread.loop = false;
					channel.sendMessage(author.getAsMention() + " ``Looping disabled.``").queue();
				} else {
					PlayerThread.loop = true;
					channel.sendMessage(author.getAsMention() + " ``Looping enabled.``").queue();
				}

				break;


			case "list":
				PlayerThread.sendPlaylist(author, channel);

				break;


			case "pause":
				if(!isAdmin(author)) {
					channel.sendMessage(author.getAsMention() + " ``Only admins can pause me.``").queue();
					return;
				}

				if(PlayerThread.isPaused()) {
					channel.sendMessage("``Continue playback ...``").queue();
					PlayerThread.setPaused(false);
				} else {
					PlayerThread.setPaused(true);
					channel.sendMessage("``Paused.\n"
							+ "Type this command again to resume.``").queue();
				}

				break;


			case "stop":
				if(isAdmin(author)) {
					stopPlayer();
				} else {
					channel.sendMessage(author.getAsMention() + " ``Only admins can stop me.``").queue();
				}

				break;


			case "uptime":
				long duration = System.currentTimeMillis() - startTime;
				channel.sendMessage(author.getAsMention() + " ``Uptime: " + durationToTimeString(duration) + "``").queue();

				break;


			case "about":
				channel.sendMessage("__**" + Values.BOT_NAME + "**__\n\n"
						+ "Version: " + Values.BOT_VERSION + "\n"
						+ "Author: " + Values.BOT_DEVELOPER + "\n"
						+ "GitHub: https://github.com/" + Values.BOT_GITHUB_REPO
						+ "\n\n"
						+ "__**Dependencies**__\n\n"
						+ "__JDA__\n"
						+ "Version: " + JDAInfo.VERSION + "\n"
						+ "GitHub: https://github.com/DV8FromTheWorld/JDA\n"
						+ "__Lavaplayer__\n"
						+ "Version: " + PlayerLibrary.VERSION + "\n"
						+ "GitHub: https://github.com/sedmelluq/lavaplayer").queue();


				if(updateChecker != null && updateChecker.isUpdateAvailable()) {
					sendUpdateMessage(false);
				}

				break;


			case "add":
				if(arg == null) {
					channel.sendMessage(author.getAsMention() + " ``Please specify what I should add to the playlist. Put it behind this command.``").queue();
					return;
				}

				join(); // try to join if not already

				if(joined) { // if successfully joined

					File inputFile = new File(arg);

					if(inputFile.isDirectory()) {
						channel.sendMessage("Adding all supported files from folder to queue ...").queue();;
						File[] files = inputFile.listFiles();
						Arrays.sort(files);
						int addesFiles = 0;
						for(File f : files) {
							if(f.isFile()) {
								PlayerThread.loadAndPlay(channel, f.getAbsolutePath(), false, true);
							}
							addesFiles++;
						}
						channel.sendMessage(author.getAsMention() + " ``Added " + addesFiles + " files.``").queue();
					} else {
						PlayerThread.loadAndPlay(channel, arg, false, false);
					}

				}

				break;


			case "play":
				if(!isAdmin(author)) {
					channel.sendMessage(author.getAsMention() + " ``Sorry, only admins can play something direct.``").queue();
					return;
				}

				if(arg == null) {
					channel.sendMessage(author.getAsMention() + " ``Please specify what I should play. Put it behind this command.``").queue();
					return;
				}

				join(); // try to join if not already

				if(joined) { // if successfully joined
					PlayerThread.loadAndPlay(channel, arg, true, false);
				}

				break;


			case "search":
				if(arg == null) {
					channel.sendMessage(author.getAsMention() + " ``Please specify a video title. Put it behind this command.``").queue();
					return;
				}

				join(); // try to join if not already

				if(joined) { // if successfully joined
					PlayerThread.loadAndPlay(channel, ("ytsearch:" + arg), true, false); // use "ytsearch:" prefix of lavaplayer
				}

				break;


			case "save":
				if(!isAdmin(author)) {
					channel.sendMessage(author.getAsMention() + " ``Sorry, only admins can save playlists.``").queue();
					return;
				}
				// arg = playlist name
				if(arg == null) {
					channel.sendMessage(author.getAsMention() + " ``Please specify a playlist name. Put it behind this command.``").queue();
					break;
				}
				if(!PlayerThread.isPlaying()) {
					channel.sendMessage(author.getAsMention() + " ``The playlist is empty, nothing to save.``").queue();
					break;
				}
				File playlistsFolder = new File(Config.getAppDir(), "playlists");
				if(!playlistsFolder.exists()) {
					if(!playlistsFolder.mkdir()) {
						channel.sendMessage(author.getAsMention() + " ``Failed to create playlists folder.``").queue();
						break;
					}
				}
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(new File(playlistsFolder, arg), Charset.forName("UTF-8"), false));
					// Write currently playing track
					writer.write(PlayerThread.getMusicManager().player.getPlayingTrack().getInfo().uri);
					writer.newLine();
					// Write upcoming tracks
					ArrayList<AudioTrack> upcoming = PlayerThread.getMusicManager().scheduler.getList();
					if(!upcoming.isEmpty()) {
						for(int i = 0; i < upcoming.size(); i++) {
							writer.write(upcoming.get(i).getInfo().uri);
							writer.newLine();
						}
					}
					// Save
					writer.close();

					channel.sendMessage(author.getAsMention() + " Playlist saved: " + arg).queue();
				} catch (Exception e) {
					channel.sendMessage(author.getAsMention() + " ``Failed to save playlist.``").queue();
				}

				break;


			case "load":
				// arg = playlist name
				if(arg == null) {
					channel.sendMessage(author.getAsMention() + " ``Please specify a playlist name. Put it behind this command.``").queue();
					break;
				}
				// Load the playlist
				try {
					File playlistFile = new File(new File(Config.getAppDir(), "playlists"), arg);
					if(!playlistFile.exists()) {
						channel.sendMessage(author.getAsMention() + " Playlist doesn't exist: " + arg).queue();
						break;
					}

					join(); // try to join if not already

					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(playlistFile), "UTF-8"));
					String line;
					while((line = bufferedReader.readLine()) != null) {
						PlayerThread.loadAndPlay(channel, line, false, true);
					}
					bufferedReader.close();

					channel.sendMessage(author.getAsMention() + " Playlist loaded: " + arg).queue();
				} catch (Exception e) {
					channel.sendMessage(author.getAsMention() + " Failed to load playlist: " + arg).queue();
				}

				break;


			default:
				channel.sendMessage(author.getAsMention() + " ``Unknown command``").queue();
				break;
			}

		}
	}

	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		a.errExit("I got kicked.");
	}

	static void stopPlayer() {
		// stop the music
		PlayerThread.stop();
		// leave the channel
		leave();
		// cancel skipping
		PlayerThread.skipping = false;
		// clear the playlist
		PlayerThread.getMusicManager().scheduler.clear();
	}

	static long timeToMS(int hours, int minutes, int seconds) {
		if(seconds > 59 || seconds < 0) {
			return -1;
		}
		if(minutes > 59 || minutes < 0) {
			return -1;
		}

		long s = (seconds + (60 * (minutes + (hours * 60))));
		return TimeUnit.SECONDS.toMillis(s);
	}

	private static String durationToTimeString(long duration) {
		TimeUnit scale = TimeUnit.MILLISECONDS;
		long days = scale.toDays(duration);
		duration -= TimeUnit.DAYS.toMillis(days);
		long hours = scale.toHours(duration);
		duration -= TimeUnit.HOURS.toMillis(hours);
		long minutes = scale.toMinutes(duration);
		duration -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = scale.toSeconds(duration);
		return String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);
	}

	static void setGame(Game game) {
		api.getPresence().setGame(game);

		Log.debug("Set game to: {}", game);
	}

	static void sendUpdateMessage(boolean toOwner) {
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

	static String getTrackName(AudioTrack track) {
		String sourceName = track.getSourceManager().getSourceName();
		if(sourceName.equals("local") || sourceName.equals("http")) {
			return track.getIdentifier();
		} else {
			return track.getInfo().title;
		}
	}

}

