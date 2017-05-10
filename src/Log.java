
public class Log {

	static void print(String msg) {
		if(Values.TESTING) {
			System.out.println("[" + Values.BOT_NAME + "-Testing] " + msg);
		} else {
			System.out.println("[" + Values.BOT_NAME + "] " + msg);
		}
	}

	static void crash(String reason) {
		if(Values.TESTING) {
			System.out.println("[" + Values.BOT_NAME + "-Testing] Crash! Reason:");
		} else {
			System.out.println("[" + Values.BOT_NAME + "] Crash! Reason:");
		}
		System.out.println(reason);
		a.errExit();
	}

}
