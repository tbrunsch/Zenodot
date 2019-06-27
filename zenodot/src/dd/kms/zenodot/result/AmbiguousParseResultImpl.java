package dd.kms.zenodot.result;

class AmbiguousParseResultImpl implements AmbiguousParseResult
{
	private final int		position;
	private final String	message;

	AmbiguousParseResultImpl(int position, String message) {
		this.position = position;
		this.message = message;
	}

	@Override
	public ParseOutcomeType getOutcomeType() {
		return ParseOutcomeType.AMBIGUOUS_RESULT;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
