package dd.kms.zenodot.result;

class ParseErrorImpl implements ParseError
{
	private final int			position;
	private final String		message;
	private final ErrorPriority errorType;
	private final Throwable		throwable;

	ParseErrorImpl(int position, String message, ErrorPriority errorType, Throwable throwable) {
		this.position = position;
		this.message = message;
		this.errorType = errorType;
		this.throwable = throwable;
	}

	@Override
	public ParseOutcomeType getOutcomeType() {
		return ParseOutcomeType.ERROR;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public ErrorPriority getErrorType() {
		return errorType;
	}

	@Override
	public Throwable getThrowable() {
		return throwable;
	}

	@Override
	public String toString() {
		return position + ": " + errorType + ": " + message + (throwable == null ? "" : " (" + throwable.getMessage() + ")");
	}
}
