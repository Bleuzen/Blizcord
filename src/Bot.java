import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.JDAInfo;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class Bot extends ListenerAdapter {

	private static JDA api;
	private static Guild guild;
	private static TextChannel controlChannel;

	private static ArrayList<Long> admins = new ArrayList<>();

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
		try {
			// config got loaded in a

			if(Config.get(Config.BOT_TOKEN).isEmpty()) {
				Log.print("You must specify a Token in the config file!");
				a.errExit();
			}

			String adms = Config.get(Config.ADMIN_IDS);
			if(!adms.isEmpty()) {
				String[] admsArr = adms.split(":");
				for(String admin : admsArr) {
					try {
						admins.add(Long.parseLong(admin));
					} catch(NumberFormatException e) {
						Log.print("Invalid admin ID: " + admin);
					}
				}
			}

			Log.print("Starting JDA ...");

			api = new JDABuilder(AccountType.BOT).setToken(Config.get(Config.BOT_TOKEN))
					//.setEnableShutdownHook(false) // default: true
					.buildBlocking();
			api.addEventListener(new Bot());

			// old shutdown hook
			/*Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
					@Override
					public void run() {
						Log.printWithoutNL("Shutting down ...");
						api.shutdown();
						Log.printRawWithNL(" Bye");
					}
				}));*/

			// test for only one server
			int guilds = api.getGuilds().size();

			if(guilds == 0) {
				//Log.print("To add the bot to your server, please visit: " + api.asBot().getInviteUrl(Permission.ADMINISTRATOR)); // start with permissions
				Log.print("To add the bot to your server, please visit: " + api.asBot().getInviteUrl());
				a.errExit();

			} else if(guilds > 1) {
				Log.print("This bot is on more than 1 server. This is currently not supported.");
				a.errExit();
			}

			guild = api.getGuilds().get(0);

			try {
				controlChannel = guild.getTextChannelsByName(Config.get(Config.CONTROL_CHANNEL), true).get(0); // true for Ignore Case
			} catch(IndexOutOfBoundsException e) {
				Log.print("There is no '" + Config.get(Config.CONTROL_CHANNEL) + "' Text Channel.");
				a.errExit();
			}

			// Init Player
			PlayerThread.init();

			// Start game update thread
			if(Boolean.valueOf(Config.get(Config.DISPLAY_SONG_AS_GAME))) {
				new Thread(new PlayerThread()).start();
			}

			// Start checking for updates
			int updateCheckInterval;
			try {
				updateCheckInterval= Integer.valueOf(Config.get(Config.UPDATE_CHECK_INTERVAL_HOURS));
			} catch (NumberFormatException e) {
				updateCheckInterval = 0;
			}
			if(updateCheckInterval > 0) {
				// First update check delayed 5 seconds, then all updateCheckInterval hours
				new Timer().schedule(new UpdateChecker(), 5000, (1000 * 3600 * updateCheckInterval));
			}

			Log.print("Successfully started.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void shutdown() {
		Log.print("Shutting down ...");
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

				if(Boolean.valueOf(Config.get(Config.SHOW_BITRATE_HINT)) && channel.getBitrate() != 96000) {
					controlChannel.sendMessage(guild.getOwner().getAsMention() + " Hint: You should set your channel's bitrate to 96kbps (highest) if you want to listen to music.").queue();
				}
			} catch(Exception e) {
				controlChannel.sendMessage("Failed to join voice channel: " + cName + "\n"
						+ "Please check your config and give me the permission to join it.").queue();
			}
		}
	}

	static void leave() {
		// only defined guild, for one server
		guild.getAudioManager().closeAudioConnection();
		joined = false;
	}

	private static boolean isAdmin(User user) {
		return user.getId().equals(guild.getOwner().getUser().getId()) || admins.contains(Long.parseLong(user.getId()));
	}


	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		String message = event.getMessage().getContent();

		if ( (event.getChannel() == controlChannel) && message.startsWith(Config.get(Config.COMMAND_PREFIX)) ) {

			String[] cmdarg = message.substring(Config.get(Config.COMMAND_PREFIX).length()).split(" ", 2);
			String cmd = cmdarg[0].toLowerCase();
			String arg;
			try {
				arg = cmdarg[1];
			} catch (IndexOutOfBoundsException e) {
				arg = null;
			}
			User author = event.getAuthor();

			switch (cmd) {
			case "help":
				event.getChannel().sendMessage(author.getAsMention() + " **Commands:**\n"
						+ "\n**Everyone:**"
						+ "```\n"
						+ "/list                           (Show the playlist)\n"
						+ "/id                             (Send your (admin-)ID)"
						+ "```"
						+ "\n**Admins:**\n"
						+ "```"
						+ "/play <file or link>            (Play given track now)\n"
						+ "/add <file, folder or link>     (Add given track to playlist)\n"
						+ "/pause                          (Pause or resume the current track)\n"
						+ "/skip (<how many songs>)        (Skip one or more songs from the playlist)\n"
						+ "/jump (<how many seconds>)      (Jump forward in the current track)\n"
						+ "/repeat (<how many times>)      (Repeat the current playlist)\n"
						+ "/stop                           (Stop the playback and clear the playlist)\n"
						+ "/version                        (Print versions)\n"
						+ "/kill                           (Kill the bot)"
						+ "```").queue();

				break;

			case "kill":
				if(isAdmin(author)) {
					event.getChannel().sendMessage("Bye").complete(); // complete(): block this thread (send the message first, than shutdown)
					shutdown();
				} else {
					event.getChannel().sendMessage(author.getAsMention() + " ``Only admins can kill me.``").queue();
				}

				break;

			case "skip":
				if(PlayerThread.isPlaying()) {
					if(isAdmin(author)) {

						int skips;
						if(arg == null) {
							skips = 1;
						} else {
							try {
								skips = Integer.valueOf(arg);
								if(skips < 1) {
									throw new NumberFormatException();
								}
							} catch(NumberFormatException e) {
								event.getChannel().sendMessage(author.getAsMention() +  " Invalid number").queue();
								return;
							}
						}

						PlayerThread.getMusicManager().scheduler.nextTrack(skips);
					} else {
						event.getChannel().sendMessage(author.getAsMention() + " ``Only admins can skip.``").queue();
					}

				} else {
					event.getChannel().sendMessage(author.getAsMention() + " ``Currently I'm not playing.``").queue();
				}

				break;

			case "jump":
				if(isAdmin(author)) {
					int seconds;
					if(arg == null) {
						seconds = 10;
					} else {
						try {
							seconds = Integer.valueOf(arg);
							if(seconds < 1) {
								throw new NumberFormatException();
							}
						} catch(NumberFormatException e) {
							event.getChannel().sendMessage(author.getAsMention() +  " Invalid number").queue();
							return;
						}
					}

					AudioTrack track = PlayerThread.getMusicManager().player.getPlayingTrack();
					track.setPosition(track.getPosition() + (1000*seconds)); // starts next track when jumping over end

				} else {
					event.getChannel().sendMessage(author.getAsMention() + " ``Only admins can jump.``").queue();
				}

				break;

			case "repeat":
				if(isAdmin(author)) {

					int repeats;
					if(arg == null) {
						repeats = 1;
					} else {
						try {
							repeats = Integer.valueOf(arg);
							if(repeats < 1) {
								throw new NumberFormatException();
							}
						} catch(NumberFormatException e) {
							event.getChannel().sendMessage(author.getAsMention() +  " Invalid number").queue();
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

						event.getChannel().sendMessage( "``Repeated the playlist" + (repeats == 1 ? ".``" : (" " + repeats + " times.``") )).queue();
					} else {
						event.getChannel().sendMessage(author.getAsMention() +  " ``The playlist is empty. There is nothing to repeat.``").queue();
					}

				} else {
					event.getChannel().sendMessage(author.getAsMention() + " ``Sorry, only admins can use the repeat command.``").queue();
				}

				break;

			case "list":
				PlayerThread.sendPlaylist(author, event.getChannel());

				break;

			case "pause":
				if(isAdmin(author)) {
					if(PlayerThread.isPaused()) {
						event.getChannel().sendMessage("Continue playback ...").queue();
						PlayerThread.setPaused(false);
					} else {
						PlayerThread.setPaused(true);
						event.getChannel().sendMessage("Paused").queue();
					}
				} else {
					event.getChannel().sendMessage(author.getAsMention() + " ``Only admins can pause me.``").queue();
				}

				break;

			case "stop":
				if(isAdmin(author)) {
					// stop the music
					PlayerThread.stop();
					// leave the channel
					leave();
					// cancel skipping
					PlayerThread.skipping = false;
					// clear the playlist
					PlayerThread.getMusicManager().scheduler.clear();
				} else {
					event.getChannel().sendMessage(author + " ``Only admins can stop me.``").queue();
				}

				break;

			case "id":
				author.getPrivateChannel().sendMessage("Your (admin-)ID: " + author.getId()).queue();

				break;

			case "version":
				event.getChannel().sendMessage(author.getAsMention() + "\n"
						+ "``"
						+ Values.BOT_NAME + ": " + Values.BOT_VERSION
						+ "\n"
						+ "JDA: " + JDAInfo.VERSION
						+ "\n"
						+ "Lavaplayer: " + PlayerLibrary.VERSION
						+ "``").queue();

				break;

			case "add":
				if(isAdmin(author)) {

					if(arg == null) {
						event.getChannel().sendMessage("Please specify what I should add to the playlist. Put it behind this command.").queue();
						return;
					}

					join(); // try to join if not already

					if(joined) { // if successfully joined

						File inputFile = new File(arg);

						if(inputFile.isDirectory()) {
							event.getChannel().sendMessage("Adding all supported files from folder to queue: " + inputFile).queue();
							File[] files = inputFile.listFiles();
							Arrays.sort(files);
							for(File f : files) {
								if(f.isFile()) {
									PlayerThread.loadAndPlay(event.getChannel(), f.getAbsolutePath(), false, true);
								}
							}
						} else {
							PlayerThread.loadAndPlay(event.getChannel(), arg, false, false);
						}

					}

				} else {
					event.getChannel().sendMessage(author.getAsMention() + " ``Sorry, only admins can add something.``").queue();
				}

				break;

			case "play":
				if(isAdmin(author)) {

					if(arg == null) {
						event.getChannel().sendMessage("Please specify what I should play. Put it behind this command.").queue();
						return;
					}

					join(); // try to join if not already

					if(joined) { // if successfully joined
						PlayerThread.loadAndPlay(event.getChannel(), arg, true, false);
					}

				} else {
					event.getChannel().sendMessage(author.getAsMention() + " ``Sorry, only admins can play something.``").queue();
				}

				break;


			default:
				event.getChannel().sendMessage(author.getAsMention() + " ``Unknown command``").queue();
				break;
			}

		}
	}

	static void setGame(Game game) {
		api.getPresence().setGame(game);
		//System.out.println("GAME UPDATE: " + game);
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

