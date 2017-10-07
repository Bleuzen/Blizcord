package com.github.bleuzen.blizcord;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;

import com.github.bleuzen.blizcord.bot.AudioPlayerThread;
import com.github.bleuzen.blizcord.bot.Bot;

public class NativeKeyListener implements org.jnativehook.keyboard.NativeKeyListener {

	private static Level loggingLevel;

	@Override
	public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
		int keyCode = nativeEvent.getKeyCode();
		switch (keyCode) {
		case NativeKeyEvent.VC_MEDIA_PLAY:
			Log.debug("NativeKeyEvent: VC_MEDIA_PLAY");
			AudioPlayerThread.togglePause();
			break;

		case NativeKeyEvent.VC_MEDIA_STOP:
			Log.debug("NativeKeyEvent: VC_MEDIA_STOP");
			Bot.stopPlayer();
			break;

		case NativeKeyEvent.VC_MEDIA_PREVIOUS:
			Log.debug("NativeKeyEvent: VC_MEDIA_PREVIOUS");
			if(AudioPlayerThread.isPlaying()) {
				AudioPlayerThread.getMusicManager().player.getPlayingTrack().setPosition(0);
			}
			break;

		case NativeKeyEvent.VC_MEDIA_NEXT:
			Log.debug("NativeKeyEvent: VC_MEDIA_NEXT");
			AudioPlayerThread.getMusicManager().scheduler.nextTrack(1);
			break;

		default:
			break;
		}
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent nativeEvent) {}

	@Override
	public void nativeKeyReleased(NativeKeyEvent nativeEvent) {}

	static void setLevel(Level l) {
		loggingLevel = l;
	}

	public static void init() {
		try {
			// Get the logger for "org.jnativehook" and set the level
			Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
			logger.setLevel(loggingLevel);

			// disable the parent handlers
			logger.setUseParentHandlers(false);

			// Init JNativeHook
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeKeyListener(new NativeKeyListener());

			Log.debug("JNativeHook initialized.");
		} catch (NativeHookException e) {
			e.printStackTrace();
		}
	}

}
