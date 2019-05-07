package dd.kms.zenodot.debug;

class ParserLogEntryImpl implements ParserLogEntry
{
	private final LogLevel	logLevel;
	private final String	context;
	private final String	message;

	ParserLogEntryImpl(LogLevel logLevel, String context, String message) {
		this.logLevel = logLevel;
		this.context = context;
		this.message = message;
	}

	@Override
	public LogLevel getLogLevel() {
		return logLevel;
	}

	@Override
	public String getContext() {
		return context;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return logLevel + ": " + context + ": " + message;
	}
}
