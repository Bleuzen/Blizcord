
public class Log {

	static void print(String msg) {
		if(Values.TESTING) {
			System.out.println("[" + Values.BOT_NAME + "-Testing] " + msg);
		} else {
			System.out.println("[" + Values.BOT_NAME + "] " + msg);
		}
	}

}
