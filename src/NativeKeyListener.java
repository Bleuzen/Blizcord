import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;

public class NativeKeyListener implements org.jnativehook.keyboard.NativeKeyListener {

	@Override
	public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
		int keyCode = nativeEvent.getKeyCode();
		switch (keyCode) {
		case NativeKeyEvent.VC_MEDIA_PLAY:
			Log.debug("NativeKeyEvent: VC_MEDIA_PLAY");
			if(PlayerThread.isPaused()) {
				PlayerThread.setPaused(false);
			} else {
				PlayerThread.setPaused(true);
			}
			break;

		case NativeKeyEvent.VC_MEDIA_STOP:
			Log.debug("NativeKeyEvent: VC_MEDIA_STOP");
			Bot.stopPlayer();
			break;

		case NativeKeyEvent.VC_MEDIA_PREVIOUS:
			Log.debug("NativeKeyEvent: VC_MEDIA_PREVIOUS");
			if(PlayerThread.isPlaying()) {
				PlayerThread.getMusicManager().player.getPlayingTrack().setPosition(0);
			}
			break;

		case NativeKeyEvent.VC_MEDIA_NEXT:
			Log.debug("NativeKeyEvent: VC_MEDIA_NEXT");
			PlayerThread.getMusicManager().scheduler.nextTrack(1);
			break;

		default:
			break;
		}
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent nativeEvent) {}

	@Override
	public void nativeKeyReleased(NativeKeyEvent nativeEvent) {}

	static void init() {
		try {
			// Init JNativeHook
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeKeyListener(new NativeKeyListener());

			Log.debug("JNativeHook initialized.");
		} catch (NativeHookException e) {
			e.printStackTrace();
		}
	}

}
