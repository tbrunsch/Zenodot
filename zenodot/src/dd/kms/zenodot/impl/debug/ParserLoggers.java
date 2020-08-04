package dd.kms.zenodot.impl.debug;

import dd.kms.zenodot.api.debug.LogLevel;
import dd.kms.zenodot.api.debug.ParserLogEntry;
import dd.kms.zenodot.api.debug.ParserLogger;

public class ParserLoggers
{
	public static ParserLogEntry createLogEntry(LogLevel logLevel, String context, String message) {
		return new ParserLogEntryImpl(logLevel, context, message);
	}

	public static ParserLogger createNullLogger() {
		return new ParserNullLogger();
	}
}
