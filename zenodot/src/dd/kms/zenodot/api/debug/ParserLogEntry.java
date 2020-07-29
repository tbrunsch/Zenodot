package dd.kms.zenodot.api.debug;

public interface ParserLogEntry
{
	LogLevel getLogLevel();
	String getContext();
	String getMessage();
}
