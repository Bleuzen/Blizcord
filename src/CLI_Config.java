import java.io.File;
import java.util.InputMismatchException;
import java.util.Scanner;

public class CLI_Config {

	private final Scanner scanner;
	private final File configFile;

	public CLI_Config(File config) {
		scanner = new Scanner(System.in);
		configFile = config;
	}

	void startSetup() {
		//TODO
		System.out.println("Hey");
		System.out.println("Your number: " + getInt("Please enter a number: "));
		System.out.println("By the way, your config is here: " + configFile.getAbsolutePath());
	}

	private int getInt(String msg) {
		System.out.print(msg);
		int r = 0;
		boolean s = false;
		do {
			try {
				r = scanner.nextInt();
				scanner.nextLine();
				s = true;
			} catch (InputMismatchException e) {
				System.out.println("Invalid input. Please try again.");
				scanner.next();
				System.out.print(msg);
			}
		} while(!s);
		return r;
	}

}
