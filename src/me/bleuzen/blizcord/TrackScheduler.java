package me.bleuzen.blizcord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

//source (edited): https://github.com/sedmelluq/lavaplayer/tree/master/demo-jda

/**
 * This class schedules tracks for the audio player. It contains the queue of
 * tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
	private final AudioPlayer player;
	private final BlockingQueue<AudioTrack> queue;

	/**
	 * @param player
	 *            The audio player this scheduler uses
	 */
	public TrackScheduler(AudioPlayer player) {
		this.player = player;
		this.queue = new LinkedBlockingQueue<>();
	}

	/**
	 * Add the next track to queue or play right away if nothing is in the
	 * queue.
	 *
	 * @param track
	 *            The track to play or add to queue.
	 */
	public void queue(AudioTrack track) {
		// Calling startTrack with the noInterrupt set to true will start the
		// track only if nothing is currently playing. If
		// something is playing, it returns false and does nothing. In that case
		// the player was already playing so this
		// track goes to the queue instead.
		if (!player.startTrack(track, true)) {
			queue.offer(track);
		}
	}

	/**
	 * Start the next track, stopping the current one if it is playing.
	 */
	private void nextTrack() {
		// Start the next track, regardless of if something is already playing
		// or not. In case queue was empty, we are
		// giving null to startTrack, which is a valid argument and will simply
		// stop the player.
		AudioTrack track = queue.poll();
		player.startTrack(track, false);
	}

	public void nextTrack(int tracks) {
		// Hey PlayerThread, I'm skipping, stop the updates!
		PlayerThread.skipping = true;

		int preSkips = tracks - 1;
		if(preSkips > 0) {
			for(int i = 0; i < preSkips; i++) {
				// queue.poll() to remove one song
				if(queue.poll() == null) {
					// give up on null, because playlist is already empty
					break;
				}
			}
		}
		// last skip, start next track
		nextTrack();
		// done skipping
		PlayerThread.skipping = false;
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		// loop
		boolean loop = PlayerThread.loop && (endReason == AudioTrackEndReason.FINISHED);
		// save old track
		AudioTrack loopTrack = null;
		if(loop) {
			loopTrack = track.makeClone();
		}

		// Only start the next track if the end reason is suitable for it
		// (FINISHED or LOAD_FAILED)
		if (endReason.mayStartNext) {
			nextTrack();
		}

		// re add track if loop is enabled
		if (loop) {
			queue(loopTrack);
		}
	}

	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		// An already playing track threw an exception (track end event will still be received separately)
		Bot.getControlChannel().sendMessage("Failed to play track: " + Utils.getTrackName(track) + "\nError message: " + exception.getMessage()).queue();
		//exception.printStackTrace();
	}

	public ArrayList<AudioTrack> getList() {
		Iterator<AudioTrack> i = queue.iterator();
		ArrayList<AudioTrack> al = new ArrayList<>();
		while(i.hasNext()) {
			al.add(i.next());
		}
		return al;
	}

	public void clear() {
		queue.clear();
	}

	public void shuffle() {
		ArrayList<AudioTrack> list = getList();
		Collections.shuffle(list);

		// clear the queue
		queue.clear();
		// readd all tracks
		for(AudioTrack track : list) {
			queue.offer(track);
		}
	}
}