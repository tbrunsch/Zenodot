package dd.kms.zenodot.debug;

public class ParserLoggers
{
	public static ParserLogEntry createLogEntry(LogLevel logLevel, String context, String message) {
		return new ParserLogEntryImpl(logLevel, context, message);
	}

	public static ParserLogger createNullLogger() {
		return new ParserNullLogger();
	}
}
