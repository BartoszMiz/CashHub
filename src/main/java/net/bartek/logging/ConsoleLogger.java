package net.bartek.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsoleLogger implements ILogger {
	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final String DEBUG_PREFIX = "\033[37;40m[DBG]\033[0m";
	private static final String INFO_PREFIX = "\033[34;40m[INF]\033[0m";
	private static final String WARNING_PREFIX = "\033[33;40m[WRN]\033[0m";
	private static final String ERROR_PREFIX = "\033[30;41m[ERR]\033[0m";

	private LogLevel minimalLogLevel;

	public ConsoleLogger() {
		this.minimalLogLevel = LogLevel.Debug;
	}

	public ConsoleLogger(LogLevel minimalLogLevel) {
		this.minimalLogLevel = minimalLogLevel;
	}

	@Override
	public void Log(LogLevel level, String message) {
		if (level.compareTo(minimalLogLevel) < 0) {
			return;
		}
		String prefix;

		switch (level) {
			case Debug:
				prefix = DEBUG_PREFIX;
				break;
			case Information:
				prefix = INFO_PREFIX;
				break;
			case Warning:
				prefix = WARNING_PREFIX;
				break;
			case Error:
				prefix = ERROR_PREFIX;
				break;
			default:
				prefix = "";
		}

		System.out.println("[" + SIMPLE_DATE_FORMAT.format(new Date()) + "] " + prefix + ": " + message);
	}

	@Override
	public void LogDebug(String message) {
		Log(LogLevel.Debug, message);
	}

	@Override
	public void LogInformation(String message) {
		Log(LogLevel.Information, message);
	}

	@Override
	public void LogWarning(String message) {
		Log(LogLevel.Warning, message);
	}

	@Override
	public void LogError(String message) {
		Log(LogLevel.Error, message);
	}
}
