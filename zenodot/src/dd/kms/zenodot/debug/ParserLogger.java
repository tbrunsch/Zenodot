package dd.kms.zenodot.debug;

/**
 * This interface is primarily meant for debugging purposes. It is the basis for all loggers
 * that need to process log messages during the parsing process. Log messages are sent in form of
 * {@link ParserLogEntry}s.<br/>
 * <br/>
 * If you want to register a logger for the parsing process, then you must specify it in the
 * {@link dd.kms.zenodot.settings.ParserSettings}.
 */
public interface ParserLogger
{
	void beginChildScope();
	void endChildScope();
	void log(ParserLogEntry entry);

	/**
	 * Can be used in unit tests to debug failed tests to track down the position where the parser
	 * behaved unexpectedly.
	 */
	int getNumberOfLoggedEntries();

	/**
	 * Can be used in unit tests to debug failed tests. Restart the failed test after calling this
	 * method and set a break point in the stop method that is called when the specified number of
	 * logged entries is reached.<br/>
	 * <br/>
	 * See the implementation of {@link ParserConsoleLogger#doStop()} and {@link ParserNullLogger#doStop()}
	 * for further information and check the unit tests to see how this feature can be used automatically.
	 */
	void stopAfter(int numLoggedEntries);
}
