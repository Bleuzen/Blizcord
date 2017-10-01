package me.bleuzen.blizcord;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import net.dv8tion.jda.core.utils.SimpleLog;

public class Log {

	private static Logger logger;

	static void error(String msg) {
		logger.error(msg);
	}

	static void warn(String msg) {
		logger.warn(msg);
	}

	static void info(String msg) {
		logger.info(msg);
	}

	static void debug(String msg) {
		logger.debug(msg);
	}

	static void debug(String format, Object... argArray) {
		logger.debug(format, argArray);
	}

	static void init() {
		if(logger != null) {
			return;
		}

		// Setup loggers of JDA, lavaplayer, ...
		setupOtherLoggers();

		// Setup bot logger
		setupOwnLogger();

		// Print first log message
		Log.debug("Logger initialized.");
	}

	private static void setupOwnLogger() {
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

		logger = (Logger) LoggerFactory.getLogger(Values.BOT_NAME);
		ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
		consoleAppender.setContext(lc);
		consoleAppender.setName("console");
		LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();

		PatternLayout layout = new PatternLayout();
		layout.setPattern("[%d{HH:mm:ss}] [%level] [%logger] %msg%n");
		layout.setContext(lc);
		layout.start();
		encoder.setLayout(layout);

		consoleAppender.setEncoder(encoder);
		consoleAppender.start();

		// disable default appender
		logger.setAdditive(false);

		// enable custom console appender
		logger.addAppender(consoleAppender);

		// set logging level
		if(a.isDebug()) {
			logger.setLevel(Level.DEBUG);
		} else {
			logger.setLevel(Level.INFO);
		}
	}

	private static void setupOtherLoggers() {
		Logger lavaplayerLogger = (Logger) LoggerFactory.getLogger("com.sedmelluq.discord.lavaplayer");

		if(a.isDebug()) {
			// set JDA logging to DEBUG
			SimpleLog.LEVEL = org.slf4j.event.Level.DEBUG;
			// set lavaplayer logging to DEBUG
			lavaplayerLogger.setLevel(ch.qos.logback.classic.Level.DEBUG);
			// set JNativeHook logging level to WARNING
			NativeKeyListener.setLevel(java.util.logging.Level.WARNING);
		} else {
			if(a.isGui()) {
				// disable JDA logging (only ERROR)
				SimpleLog.LEVEL = org.slf4j.event.Level.ERROR; //TODO: Still prints DEBUG messages (JDA bug?)
				// disable lavaplayer logging
				lavaplayerLogger.setLevel(ch.qos.logback.classic.Level.OFF);
				// set JNativeHook logging level to OFF
				NativeKeyListener.setLevel(java.util.logging.Level.OFF);
			} else {
				// set JDA logging to WARN
				SimpleLog.LEVEL = org.slf4j.event.Level.WARN; //TODO: Still prints DEBUG messages (JDA bug?)
				// set lavaplayer logging to WARN
				lavaplayerLogger.setLevel(ch.qos.logback.classic.Level.WARN);
				// set JNativeHook logging level to WARNING
				NativeKeyListener.setLevel(java.util.logging.Level.WARNING);
			}
		}
	}

}
