package dd.kms.zenodot.result;

/**
 * An instance of this interface is returned if an error has been encountered when parsing
 * a certain (sub-) expression.
 */
public interface ParseError extends ParseOutcome
{
	/**
	 * The priority of error. The lower the ordinal, the higher the priority. The error priority is used to
	 * decide which error(s) to propagate if all parsers return an error.
	 */
	enum ErrorPriority implements Comparable<ErrorPriority>
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

	String getMessage();
	ErrorPriority getErrorType();
	Throwable getThrowable();
}
