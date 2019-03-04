package dd.kms.zenodot.result;

/**
 * An instance of this class is returned if an error has been encountered when parsing
 * a certain (sub-) expression.
 */
public class ParseError implements ParseResultIF
{
	/**
	 * The type of error. The lower the ordinal, the higher the priority.
	 */
	public enum ErrorType implements Comparable<ErrorType>
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
		 * The expression can be parsed syntactically by the current parser, but this parser detected
		 * that the expression is semantically incorrect. Examples are:
		 * <ul>
		 *     <li>Access of a non-static field or method via a class,</li>
		 *     <li>Array access of a non-array type</li>
		 *     <li>Calling a method with arguments with wrong types</li>
		 * </ul>
		 */
		SEMANTIC_ERROR,

		/**
		 * The current parser might be the correct parser for the expression, but due to a syntax error
		 * it cannot parse the expression.
		 */
		SYNTAX_ERROR,

		/**
		 * The current parser is (probably) the wrong parser for parsing the expression.
		 */
		WRONG_PARSER;
	};

	private final int		position;
	private final String	message;
	private final ErrorType	errorType;
	private final Throwable	throwable;

	public ParseError(int position, String message, ErrorType errorType) {
		this(position, message, errorType, null);
	}

	public ParseError(int position, String message, ErrorType errorType, Throwable throwable) {
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

	public ErrorType getErrorType() {
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
