package com.github.bleuzen.blizcord.bot;
import java.io.File;
import java.util.Arrays;

import com.github.bleuzen.blizcord.Config;
import com.github.bleuzen.blizcord.Log;
import com.github.bleuzen.blizcord.Utils;
import com.github.bleuzen.blizcord.Values;
import com.github.bleuzen.blizcord.a;
import com.github.bleuzen.blizcord.gui.GUI_Main;
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

public class AudioPlayerThread implements Runnable {

	// currently everything for only 1 server (static)

	static boolean skipping;

	public static boolean loop;

	private static AudioPlayerManager playerManager;

	private static GuildMusicManager musicManager;

	static void init() {
		playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		AudioSourceManagers.registerLocalSource(playerManager);

		initGuildAudioPlayer(Bot.getGuild());

		Log.debug("Initialized audio player.");
	}

	public static GuildMusicManager getMusicManager() {
		return musicManager;
	}

	private static synchronized void initGuildAudioPlayer(Guild guild) {
		if (musicManager == null) {
			musicManager = new GuildMusicManager(playerManager);
		}

		guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

		// try to set the starting volume
		setVolume(Config.get(Config.STARTING_VOLUME));
	}

	private static void loadAndPlay(final MessageChannel channel, final String trackUrl, boolean direct, boolean quiet) {
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
					channel.sendMessage("Now playing: ``" + Utils.getTrackName(track) + "``").queue();

				} else {

					addToPlaylist(track);
					if(!quiet) {
						channel.sendMessage("Added track to queue: ``" + Utils.getTrackName(track) + "``").queue();
					}

				}

			}

			@Override
			public void playlistLoaded(AudioPlaylist playlist) {

				if(direct) {

					AudioTrack directTrack = playlist.getTracks().get(0);
					playDirect(directTrack, 0);
					channel.sendMessage("Now playing: ``" + Utils.getTrackName(directTrack) + "``").queue();

				} else {

					if(trackUrl.startsWith(Values.SEARCH_PREFIX_YOUTUBE)) {

						AudioTrack firstSearchResult = playlist.getTracks().get(0);
						addToPlaylist(firstSearchResult);
						if(!quiet) {
							channel.sendMessage("Added track to queue: ``" + Utils.getTrackName(firstSearchResult) + "``").queue();
						}

					} else {

						for (AudioTrack track : playlist.getTracks()) {
							addToPlaylist(track);
						}
						if(!quiet) {
							channel.sendMessage("Added playlist to queue: ``" + playlist.getName() + "``").queue();
						}

					}
				}

			}

			@Override
			public void noMatches() {
				if(trackUrl.startsWith(Values.SEARCH_PREFIX_YOUTUBE)) {
					channel.sendMessage("No search results for: ``" + trackUrl.substring(Values.SEARCH_PREFIX_YOUTUBE.length()) + "``").queue();
					return;
				}

				channel.sendMessage("Nothing found by: ``" + trackUrl + "``").queue();
			}

			@Override
			public void loadFailed(FriendlyException exception) {
				if(!quiet) {
					channel.sendMessage("Could not play: ``" + exception.getMessage() + "``").queue();
				}
			}
		});
	}


	public static void addToPlaylist(String arg, boolean quiet) {
		Bot.joinVoiceChannel(); // try to join if not already

		// Check if successfully joined
		if(!Bot.joined) {
			return;
		}

		File inputFile = new File(arg);
		if(inputFile.isDirectory()) {
			Bot.getControlChannel().sendMessage("Adding all supported files from folder to queue ...").queue();;
			File[] files = inputFile.listFiles();
			Arrays.sort(files);
			int addedFiles = 0;
			for(File f : files) {
				if(f.isFile()) {
					loadAndPlay(Bot.getControlChannel(), f.getAbsolutePath(), false, true);
					addedFiles++;
				}
			}
			Bot.getControlChannel().sendMessage("``Added " + addedFiles + " files.``").queue();
		} else {
			loadAndPlay(Bot.getControlChannel(), arg, false, quiet);
		}
	}

	public static void playDirect(String arg, boolean quiet) {
		Bot.joinVoiceChannel(); // try to join if not already

		// Check if successfully joined
		if(!Bot.joined) {
			return;
		}

		loadAndPlay(Bot.getControlChannel(), arg, true, quiet);
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

	public static void addToPlaylist(AudioTrack track) {
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

	public static void setPaused(boolean p) {
		musicManager.player.setPaused(p);

		if(a.isGui()) {
			GUI_Main.settglbtnPauseSelected(p);
		}
	}

	public static boolean isPaused() {
		return musicManager.player.isPaused();
	}

	public static void togglePause() {
		setPaused(!isPaused());
	}

	public static boolean isPlaying() {
		return musicManager.player.getPlayingTrack() != null;
	}

	public static int setVolume(String v) {
		if(!Config.getBoolean(Config.ALLOW_CUSTOM_VOLUME)) {
			return Values.SET_VOLUME_ERROR_CUSTOM_VOLUME_NOT_ALLOWED;
		}

		int volume;
		try {
			volume = Integer.parseInt(v);

			if(volume > 100 || volume < 0) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException e) {
			Log.debug("Invalid volume: {}", v);
			return Values.SET_VOLUME_ERROR_INVALID_NUMBER;
		}

		musicManager.player.setVolume(volume);
		Log.debug("Player volume set to: {}", musicManager.player.getVolume());
		return Values.SET_VOLUME_SUCCESSFULLY;
	}


	/* UPDATE GAME */
	@Override
	public void run() {
		final int sleepTime = 200; // update game with 5 fps
		final int updatesDelay = 12000; // 12 seconds delay between game updates (Discord only allows to update the game 5 times per minute)

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
					game = Game.playing(Utils.getTrackName(currentTrack));
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
