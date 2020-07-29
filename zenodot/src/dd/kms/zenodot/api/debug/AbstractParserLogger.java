package dd.kms.zenodot.api.debug;

public abstract class AbstractParserLogger implements ParserLogger
{
	private int 	indent 						= 0;
	private int		numLoggedEntries			= 0;
	private int		numLoggedEntriesToStopAfter	= -1;

	protected abstract void doLog(ParserLogEntry entry, int indent);
	protected abstract void doStop();

	@Override
	public void beginChildScope() {
		indent++;
	}

	@Override
	public void endChildScope() {
		indent--;
	}

	@Override
	public void log(ParserLogEntry entry) {
		numLoggedEntries++;

		doLog(entry, indent);

		if (numLoggedEntries == numLoggedEntriesToStopAfter) {
			doStop();
		}
	}

	@Override
	public int getNumberOfLoggedEntries() {
		return numLoggedEntries;
	}

	@Override
	public void stopAfter(int numLoggedEntries) {
		this.numLoggedEntriesToStopAfter = numLoggedEntries;
	}
}
