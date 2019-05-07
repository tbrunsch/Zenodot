package dd.kms.zenodot.debug;

public interface ParserLogEntry
{
	LogLevel getLogLevel();
	String getContext();
	String getMessage();
}
