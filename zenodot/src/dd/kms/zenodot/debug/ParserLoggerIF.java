package dd.kms.zenodot.debug;

public interface ParserLoggerIF
{
	void beginChildScope();
	void endChildScope();
	void log(ParserLogEntry entry);

	int getNumberOfLoggedEntries();
	void stopAfter(int numLoggedEntries);
}
