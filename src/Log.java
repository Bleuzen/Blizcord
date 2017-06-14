import net.dv8tion.jda.core.utils.SimpleLog;
import net.dv8tion.jda.core.utils.SimpleLog.Level;

public class Log {

	static void print(String msg) {
		// only print if JDA logging is enabled
		if(!SimpleLog.LEVEL.equals(Level.OFF)) {
			System.out.println("[" + Values.BOT_NAME + "] " + msg);
		}
	}

	static void debug(String msg) {
		if(a.isDebug()) {
			System.out.println("[" + Values.BOT_NAME + "] [Debug] " + msg);
		}
	}

}
