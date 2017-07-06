import java.util.ArrayList;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class PlayerThread implements Runnable {

	// currently everything for only 1 server (static)

	static boolean skipping;

	static boolean loop;

	private static AudioPlayerManager playerManager;

	private static GuildMusicManager musicManager;

	static void init() {
		playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);

		// Set custom OutputFormat
		//playerManager.getConfiguration().setOutputFormat(new AudioDataFormat(2, 48000, 960, AudioDataFormat.Codec.OPUS));
		// Default:
		//playerManager.getConfiguration().setOutputFormat(StandardAudioDataFormats.DISCORD_OPUS);
		// Currently lavaplayer doesn't play anything with another bitrate than 960. Maybe this is a bug. I asked the developer. Waiting for an answer ...

		initGuildAudioPlayer(Bot.getGuild());

		Log.debug("Initialized audio player.");
	}

	static GuildMusicManager getMusicManager() {
		return musicManager;
	}

	private static synchronized void initGuildAudioPlayer(Guild guild) {
		if (musicManager == null) {
			musicManager = new GuildMusicManager(playerManager);
		}

		guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

		// try to set volume
		try {
			musicManager.player.setVolume(Integer.parseInt(Config.get(Config.VOLUME)));
			Log.debug("Volume set to: {}", musicManager.player.getVolume());
		} catch (NumberFormatException e) {
			a.errExit("Invalid volume", Values.EXIT_CODE_RESTART_GUI);
		}
	}

	static void sendPlaylist(User user, MessageChannel channel) {
		if(isPlaying()) {
			StringBuilder toSend = new StringBuilder(user.getAsMention() + ""
					+ " Currently playing:"
					+ "\n"
					+ "```"
					+ Bot.getTrackName(musicManager.player.getPlayingTrack())
					+ "```"
					+ "\n");
			if(musicManager.scheduler.getList().size() > 0) {
				toSend.append("Upcoming songs:"
						+ "\n"
						+ "```");
				ArrayList<AudioTrack> list = musicManager.scheduler.getList();
				for(int i = 0; i < list.size(); i++) {
					toSend.append("\n" + Bot.getTrackName(list.get(i)));
				}
				toSend.append("```");
			} else {
				toSend.append("There are no upcoming songs.");
			}
			if(toSend.length() > 2000) { // Discord's message length limit is 2000
				final String ending = "...```";
				toSend.setLength((2000 - ending.length()));
				toSend.append(ending);
			}
			channel.sendMessage(toSend.toString()).queue();
		} else {
			// because gets cleared on stop
			// isPlaying() > else:
			channel.sendMessage(user.getAsMention() + " ``The playlist is empty.``").queue();
		}
	}

	static void loadAndPlay(final MessageChannel channel, final String trackUrl, boolean direct, boolean quiet) {
		Log.debug("Loading track ... play direct: {}; URL: {}", direct, trackUrl);

		playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {

				if(direct) {
					if(track.getSourceManager().getSourceName().equals("youtube")) {
						playDirect(track, getYouTubeStartTimeMS(trackUrl));
					} else {
						playDirect(track, 0);
					}

					// quiet check not needed, since there will be never more than one track / message
					channel.sendMessage("Now playing: " + Bot.getTrackName(track)).queue();
				} else {
					play(track);
					if(!quiet) {
						channel.sendMessage("Added track to queue: " + Bot.getTrackName(track)).queue();
					}
				}

			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {

				if(direct) {
					AudioTrack directTrack = playlist.getTracks().get(0);
					playDirect(directTrack, 0);
					channel.sendMessage("Now playing: " + Bot.getTrackName(directTrack)).queue();
				} else {
					for (AudioTrack track : playlist.getTracks()) {
						play(track);
					}
					if(!quiet) {
						channel.sendMessage("Added playlist to queue: " + playlist.getName()).queue();
					}
				}

			}

			@Override
			public void noMatches() {
				channel.sendMessage("Nothing found by: " + trackUrl).queue();
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				if(!quiet) {
					channel.sendMessage("Could not play: " + exception.getMessage()).queue();
				}
			}
		});
	}

	private static long getYouTubeStartTimeMS(String trackUrl) {
		if(trackUrl.contains("youtu.be")) {
			trackUrl = trackUrl.replace("?t=", "&t="); // "?" used in youtu.be links
		}

		if (trackUrl.indexOf("&t=") == -1) {
			return 0;
		}

		int ms = 0;
		trackUrl = trackUrl.substring(trackUrl.indexOf("&t=") + 3);
		int ti = 0;
		for (int i = 0; i < trackUrl.length(); i++) {
			String sub = trackUrl.substring(i, i+1);
			if (sub.equalsIgnoreCase("h")) {
				ms += ti * 360000;
				ti = 0;
			} else if (sub.equalsIgnoreCase("m")) {
				ms += ti * 60000;
				ti = 0;
			} else if (sub.equalsIgnoreCase("s")) {
				ms += ti * 1000;
				ti = 0;
			} else {
				ti *= 10;
				ti += Integer.parseInt(sub);
			}
		}
		return ms;
	}

	static void play(AudioTrack track) {
		//connectToFirstVoiceChannel(guild.getAudioManager());

		musicManager.scheduler.queue(track);
	}

	static void playDirect(AudioTrack track, long startingPositionMS) {
		//connectToFirstVoiceChannel(guild.getAudioManager());
		musicManager.player.startTrack(track, false);

		if(startingPositionMS > 0) {
			musicManager.player.getPlayingTrack().setPosition(startingPositionMS);
		}
	}

	static void stop() {
		musicManager.player.stopTrack();
	}

	static void setPaused(boolean p) {
		musicManager.player.setPaused(p);
	}

	static boolean isPaused() {
		return musicManager.player.isPaused();
	}

	static boolean isPlaying() {
		return musicManager.player.getPlayingTrack() != null;
	}



	/* UPDATE GAME */
	@Override
	public void run() {
		final int sleepTime = 200; // update game with 5 fps
		final int updatesDelay = 12000; // 12 seconds delay between game updates (Discord can only update the game 5 times per minute), see: https://github.com/DV8FromTheWorld/JDA/issues/323

		Game lastGame = null;
		int updateDelay = 0;

		while(true) {
			do { // sleep at least one time
				try {
					Thread.sleep(sleepTime);
					if(updateDelay > 0) {
						updateDelay -= sleepTime;
					}
				} catch (InterruptedException e) {
					//e.printStackTrace();
				}
			} while(skipping); // sleep again during song skipping to avoid useless game updates

			if(updateDelay <= 0) { // check if we can update again

				// Update game if needed
				Game game;
				AudioTrack currentTrack = musicManager.player.getPlayingTrack();
				if(currentTrack == null) {
					game = null;
				} else {
					game = Game.of(Bot.getTrackName(currentTrack));
				}

				if(lastGame == null) {
					if(game != null) {
						Bot.setGame(game);
						lastGame = game;
						updateDelay = updatesDelay;
						//Log.print("UPDATE GAME LGN: " + game.getName());
					}
				} else {
					if(game == null) {
						Bot.setGame(null);
						lastGame = null;
						updateDelay = updatesDelay;
						//Log.print("UPDATE GAME NULL");
					} else {
						if(!lastGame.getName().equals(game.getName())) {
							Bot.setGame(game);
							lastGame = game;
							updateDelay = updatesDelay;
							//Log.print("UPDATE GAME NE: " + game.getName());
						}
					}
				}

			}

		}
	}
	/* UPDATE GAME */

}
