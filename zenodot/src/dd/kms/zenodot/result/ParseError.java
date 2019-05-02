package dd.kms.zenodot.result;

/**
 * An instance of this class is returned if an error has been encountered when parsing
 * a certain (sub-) expression.
 */
public class ParseError implements ParseResult
{
	/**
	 * The priority of error. The lower the ordinal, the higher the priority. The error priority is used to
	 * decide which error(s) to propagate if all parsers return an error.
	 */
	public enum ErrorPriority implements Comparable<ErrorPriority>
	{
		/**
		 * The expression can be parsed with the current parser, but due to an unexpected internal state
		 * in the parsing framework the parser must stop parsing.
		 */
		INTERNAL_ERROR,

		/**
		 * The expression could be parsed correctly by the current parser, but an exception occurred
		 * during evaluation.
		 */
		EVALUATION_EXCEPTION,

		/**
		 * The parser is the right one, but a syntax or a semantic error has been encountered.
		 */
		RIGHT_PARSER,

		/**
		 * The current parser might be the right parser for the expression.
		 */
		POTENTIALLY_RIGHT_PARSER,

		/**
		 * The current parser is (probably) the wrong parser for parsing the expression.
		 */
		WRONG_PARSER;
	};

	private final int		position;
	private final String	message;
	private final ErrorPriority errorType;
	private final Throwable	throwable;

	public ParseError(int position, String message, ErrorPriority errorType) {
		this(position, message, errorType, null);
	}

	public ParseError(int position, String message, ErrorPriority errorType, Throwable throwable) {
		this.position = position;
		this.message = message;
		this.errorType = errorType;
		this.throwable = throwable;
	}

	@Override
	public ParseResultType getResultType() {
		return ParseResultType.PARSE_ERROR;
	}

	@Override
	public int getPosition() {
		return position;
	}

	public String getMessage() {
		return message;
	}

	public ErrorPriority getErrorType() {
		return errorType;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	@Override
	public String toString() {
		return position + ": " + errorType + ": " + message + (throwable == null ? "" : " (" + throwable.getMessage() + ")");
	}
}
