import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.UIManager;

public class a {

	private static boolean gui;

	public static void main(String[] args) {
		if(args.length > 0 && args[0].equalsIgnoreCase("--gui")) {
			gui = true;
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				GUI frame = new GUI();
				frame.setVisible(true);
			} catch (Exception e) {
				System.out.print("[" + Values.BOT_NAME + "] Failed to start GUI: " + e.getMessage());
				errExit();
			}
		} else {
			launch(args);
		}
	}

	static void launch(String[] args) {

		// Don't use Log before libraries are checked, because ProGuard doesn't like it
		System.out.println(Values.BOT_NAME + " v" + Values.BOT_VERSION + " by " + Values.BOT_DEVELOPER);
		System.out.println("[" + Values.BOT_NAME + "] Starting ...");

		if(Values.GENERATE_CHECKSUMS) {
			try {
				Config.init(new File("testingConfig.txt"));
				generateLibChecksums();
				System.err.println("NEW CHECKSUMS: " + Values.CHECKSUMS_FILE_NAME + ".properties" + ": " + getFileChecksum(new File("src/" + Values.CHECKSUMS_FILE_NAME + ".properties")));
				System.exit(0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if(!Values.TESTING) {
			System.out.print("[" + Values.BOT_NAME + "] Checking libraries ...");

			File libdir = new File(Values.BOT_NAME + "_lib");
			if(libdir.isDirectory()) {

				try {

					InputStream is = a.class.getResourceAsStream(Values.CHECKSUMS_FILE_NAME + ".properties");
					if(!getChecksum(is).equals(Values.CHECKSUMS_FILE_HASH)) {
						throw new Exception();
					}

					ResourceBundle checksums = ResourceBundle.getBundle(Values.CHECKSUMS_FILE_NAME); // here without ".properties"
					Enumeration<String> keys = checksums.getKeys();

					while(keys.hasMoreElements()) {
						File file = new File(libdir, keys.nextElement());

						if(!checksums.getString(file.getName()).equals(getFileChecksum(file))) {
							System.out.println();
							errExit("The md5 sum of '" + file + "' is not correct.");
						}
					}

				} catch(Exception e) {
					// Libs check Error
					System.out.println(" Error");
					errExit();
				}

			} else {
				System.out.println();
				errExit("The folder '" + Values.BOT_NAME + "_lib" + "' doesn't exist here. Make sure you changed your working directory first.");
			}

			// Libs ok, print newline and there version in Bot class
			System.out.println(" OK");
		}


		// init config
		File configFile;
		if(Values.TESTING) {
			configFile = new File("testingConfig.txt");
		} else {
			if(args.length > 1 && args[0].equalsIgnoreCase("--config")) {
				configFile = new File(args[1]);
			} else {
				configFile = new File(Values.DEFAULT_CONFIG);
			}
		}

		System.out.println("[" + Values.BOT_NAME + "] Config: " + configFile.getAbsolutePath());

		// load the config file
		if(!Config.init(configFile)) {
			errExit("[" + Values.BOT_NAME + "] Failed to load config.");
		}


		Bot.start();
	}

	static void errExit() {
		errExit(null);
	}

	static void errExit(String msg) {
		if(gui) {
			GUI.onErrExit(msg);
		} else {
			if(Values.TESTING) {
				System.out.println("[" + Values.BOT_NAME + "-Testing] Crash! Reason:");
			} else {
				System.out.println("[" + Values.BOT_NAME + "] Crash! Reason:");
			}
			System.out.println(msg);
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
		System.exit(1);
	}

	static String getChecksum(InputStream is) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");

			// Create byte array to read data in chunks
			byte[] byteArray = new byte[1024];
			int bytesCount = 0;

			// Read file data and update in message digest
			while ((bytesCount = is.read(byteArray)) != -1) {
				digest.update(byteArray, 0, bytesCount);
			}

			// close the stream; We don't need it now.
			is.close();

			// Get the hash's bytes
			byte[] bytes = digest.digest();

			// This bytes[] has bytes in decimal format;
			// Convert it to hexadecimal format
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}

			// return complete hash
			return sb.toString();
		} catch(Exception e) {
			return null;
		}
	}

	static String getFileChecksum(File f) {
		try {
			return getChecksum(new FileInputStream(f));
		} catch(Exception e) {
			return null;
		}
	}

	private static void generateLibChecksums() throws Exception {

		Properties p = new Properties();
		File[] files = new File(Config.get("TESTING_LIBS_FOLDER")).listFiles();
		for(File f : files) {
			p.setProperty(f.getName(), getFileChecksum(f));
		}
		File outProps = new File("src/" + Values.CHECKSUMS_FILE_NAME + ".properties");
		try {
			OutputStream os = new FileOutputStream(outProps);
			p.store(os, null);
		} catch(Exception e) {
			e.printStackTrace();
		}

		FileReader fr = new FileReader(outProps);
		String s;
		String totalStr = "";
		boolean firstLine = true;
		try (BufferedReader br = new BufferedReader(fr)) {
			while ((s = br.readLine()) != null) {
				if(!s.startsWith("#") && !s.startsWith(" ")) {
					if(firstLine) {
						firstLine = false;
					} else {
						totalStr += "\n";
					}
					totalStr += s;
				}
			}
			FileWriter fw = new FileWriter(outProps);
			fw.write(totalStr);
			fw.close();
		}

	}

}
