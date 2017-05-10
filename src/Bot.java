import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

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

	public static void start() {
		try {
			// config got loaded in a

			if(Config.get(Config.BOT_TOKEN).isEmpty()) {
				Log.print("You must specify a Token in the config file or as argument! You can get it here: https://discordapp.com/developers/applications/me");
				a.errExit();
			}

			final String adms = Config.get(Config.ADMIN_IDS);
			if(!adms.isEmpty() && !adms.startsWith("#")) {
				final String[] admsArr = adms.split(":");
				for(final String admin : admsArr) {
					try {
						admins.add(Long.parseLong(admin));
					} catch(final NumberFormatException e) {
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
			final int guilds = api.getGuilds().size();

			if(guilds == 0) {
				// start with permissions
				//Log.print("To add the bot to your server, please visit: " + api.asBot().getInviteUrl(Permission.ADMINISTRATOR));

				// print invite URL without permissions
				Log.print("To add the bot to your server, please visit: " + api.asBot().getInviteUrl());
				a.errExit();

			} else if(guilds > 1) {
				Log.print("This bot is on more than 1 server. This is currently not supported.");
				a.errExit();
			}

			guild = api.getGuilds().get(0);

			try {
				// set control channel, true for Ignore Case
				controlChannel = guild.getTextChannelsByName(Config.get(Config.CONTROL_CHANNEL), true).get(0);
			} catch(final IndexOutOfBoundsException e) {
				Log.print("There is no '" + Config.get(Config.CONTROL_CHANNEL) + "' Text Channel.");
				a.errExit();
			}

			// Init Player
			PlayerThread.init();

			// Start game update thread
			if(Boolean.valueOf(Config.get(Config.DISPLAY_SONG_AS_GAME))) {
				new Thread(new PlayerThread()).start();
			}

			// Check for updates
			if(Boolean.valueOf(Config.get(Config.CHECK_FOR_UPDATES))) {
				new Thread(new UpdateChecker()).start();
			}

			Log.print("Successfully started.");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	static void shutdown() {
		Log.print("Shutting down ...");

		// done by shutdown hook of JDA
		//api.shutdown();

		System.exit(0);
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
		final String message = event.getMessage().getContent();

		if ( (event.getChannel() == controlChannel) && message.startsWith(Config.get(Config.COMMAND_PREFIX)) ) {

			final String[] cmdarg = message.substring(Config.get(Config.COMMAND_PREFIX).length()).split(" ", 2);
			final String cmd = cmdarg[0].toLowerCase();
			String arg;
			try {
				arg = cmdarg[1];
			} catch (final IndexOutOfBoundsException e) {
				arg = null;
			}
			final User author = event.getAuthor();

			switch (cmd) {
			case "help":
				event.getChannel().sendMessage(author.getAsMention() + " **Commands:**\n"
						+ "\n**Everyone:**" // "\n" not here
						+ "```\n" // after "```", don't ask me why
						+ "list\n"
						+ "id"
						+ "```"
						+ "\n**Admins:**\n"
						+ "```"
						+ "play <file, folder or link>\n"
						+ "pause\n"
						+ "skip (<how many songs>)\n"
						+ "jump (<how many seconds>)\n"
						+ "repeat (<how many times>)\n"
						+ "stop\n"
						+ "version\n"
						+ "kill"
						+ "```").queue();

				break;

			case "kill":
				if(isAdmin(author)) {
					event.getChannel().sendMessage("Bye").complete(); // complete(): block this thread (send the message first, than shutdown)
					shutdown();
				} else {
					event.getChannel().sendMessage(author.getAsMention() + " ``Only admins may kill me.``").queue();
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
							} catch(final NumberFormatException e) {
								event.getChannel().sendMessage(author.getAsMention() +  " Invalid number").queue();
								return;
							}
						}

						PlayerThread.getMusicManager().scheduler.nextTrack(skips);
					} else {
						event.getChannel().sendMessage(author.getAsMention() + " ``Only admins may skip.``").queue();
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
						} catch(final NumberFormatException e) {
							event.getChannel().sendMessage(author.getAsMention() +  " Invalid number").queue();
							return;
						}
					}

					final AudioTrack track = PlayerThread.getMusicManager().player.getPlayingTrack();
					track.setPosition(track.getPosition() + (1000*seconds)); // starts next track when jumping over end

				} else {
					event.getChannel().sendMessage(author.getAsMention() + " ``Only admins may jump.``").queue();
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
						} catch(final NumberFormatException e) {
							event.getChannel().sendMessage(author.getAsMention() +  " Invalid number").queue();
							return;
						}
					}

					if(PlayerThread.isPlaying()) {

						final ArrayList<AudioTrack> songs = new ArrayList<>();
						songs.add(PlayerThread.getMusicManager().player.getPlayingTrack());
						final ArrayList<AudioTrack> upcoming = PlayerThread.getMusicManager().scheduler.getList();
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

						event.getChannel().sendMessage("``Repeated the playlist " + repeats + " times.``").queue();
					} else {
						event.getChannel().sendMessage(author.getAsMention() +  " ``The playlist is empty. There is nothing to repeat.``").queue();
					}

				} else {
					event.getChannel().sendMessage(author.getAsMention() + " ``Sorry, only admins may use the repeat command.``").queue();
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
					event.getChannel().sendMessage(author.getAsMention() + " ``Only admins may pause me.``").queue();
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
					event.getChannel().sendMessage(author + " ``Only admins may stop me.``").queue();
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

			case "play":
				if(isAdmin(author)) {

					if(arg == null) {
						event.getChannel().sendMessage("Please specify what I should play. Put it behind this command.").queue();
						return;
					}

					if (!joined) {
						final VoiceChannel channel = event.getGuild().getVoiceChannels().stream().filter(vChan -> vChan.getName().equalsIgnoreCase(Config.get(Config.VOICE_CHANNEL))).findFirst().orElse(null);
						try {
							event.getGuild().getAudioManager().openAudioConnection(channel);
							joined = true;
						} catch(final NullPointerException e) {
							Log.print("Failed to join Voice Channel.");
						}
					}

					if(joined) {

						final File inputFile = new File(arg);
						if(inputFile.isDirectory()) {
							event.getChannel().sendMessage("Adding folder to queue: " + inputFile).queue();
							final File[] files = inputFile.listFiles();
							Arrays.sort(files);
							for(final File f : files) {
								if(f.isFile()) {
									PlayerThread.loadAndPlay(event.getChannel(), f.getAbsolutePath(), true);
								}
							}
						} else {
							PlayerThread.loadAndPlay(event.getChannel(), arg, false);
						}

					}

				} else {
					event.getChannel().sendMessage(author.getAsMention() + " ``Sorry, only admins may play something.``").queue();
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
	}

	static String getTrackName(AudioTrack track) {
		final String sourceName = track.getSourceManager().getSourceName();
		if(sourceName.equals("local") || sourceName.equals("http")) {
			return track.getIdentifier();
		} else {
			return track.getInfo().title;
		}
	}

}

