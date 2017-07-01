import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;

public class NativeKeyListener implements org.jnativehook.keyboard.NativeKeyListener {

	@Override
	public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
		int keyCode = nativeEvent.getKeyCode();
		switch (keyCode) {
		case NativeKeyEvent.VC_MEDIA_PLAY:
			Log.debug("VC_MEDIA_PLAY");
			break;

		case NativeKeyEvent.VC_MEDIA_STOP:
			Log.debug("VC_MEDIA_STOP");
			break;

		case NativeKeyEvent.VC_MEDIA_PREVIOUS:
			Log.debug("VC_MEDIA_PREVIOUS");
			break;

		case NativeKeyEvent.VC_MEDIA_NEXT:
			Log.debug("VC_MEDIA_NEXT");
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
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeKeyListener(new NativeKeyListener());

			// Setup logger
			Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
			if(a.isDebug()) {
				// Only display warnings and errors
				logger.setLevel(Level.WARNING);
			} else {
				// Disable all console output
				logger.setLevel(Level.OFF);
			}
			logger.setUseParentHandlers(false);

			Log.debug("JNativeHook initialized.");
		} catch (NativeHookException e) {
			e.printStackTrace();
		}
	}

}
