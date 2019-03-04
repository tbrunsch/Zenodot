package dd.kms.zenodot.debug;

public class ParserNullLogger extends AbstractParserLogger
{
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
