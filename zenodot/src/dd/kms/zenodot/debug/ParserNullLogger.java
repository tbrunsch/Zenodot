package dd.kms.zenodot.debug;

/**
 * Logger that ignores all log entries
 */
class ParserNullLogger extends AbstractParserLogger
{
	@Override
	public boolean ignoresLogMessages() {
		return true;
	}

	@Override
	void doLog(ParserLogEntry entry, int indent) {
		/* do nothing */
	}

	@Override
	void doStop() {
		// set a break point here to stop at the desired point in time
		int a = 0;
	}
}
