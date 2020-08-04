package dd.kms.zenodot.impl.debug;

import dd.kms.zenodot.api.debug.AbstractParserLogger;
import dd.kms.zenodot.api.debug.ParserLogEntry;

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
	protected void doLog(ParserLogEntry entry, int indent) {
		/* do nothing */
	}

	@Override
	protected void doStop() {
		// set a break point here to stop at the desired point in time
		int a = 0;
	}
}
