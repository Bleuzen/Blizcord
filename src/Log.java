import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;

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

	static void init(boolean debug) {
		if(logger != null) {
			return;
		}

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

		logger = (Logger) LoggerFactory.getLogger(Values.BOT_NAME);
		ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
		consoleAppender.setContext(lc);
		consoleAppender.setName("console");
		LayoutWrappingEncoder<ILoggingEvent> encoder = new LayoutWrappingEncoder<>();

		PatternLayout layout = new PatternLayout();
		layout.setPattern("[%d{HH:mm:ss}] [%logger] [%level] %msg%n");
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
		if(debug) {
			logger.setLevel(ch.qos.logback.classic.Level.DEBUG);
		} else {
			logger.setLevel(ch.qos.logback.classic.Level.INFO);
		}
	}

}
