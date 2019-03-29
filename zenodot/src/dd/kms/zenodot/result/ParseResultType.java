package dd.kms.zenodot.result;

/**
 * Describes the type of a concrete {@link ParseResult}
 */
public enum ParseResultType
{
	/**
	 * Result type of {@link CompletionSuggestions}. The result contains completion suggestions.
	 */
	COMPLETION_SUGGESTIONS,

	/**
	 * Result type of {@link ObjectParseResult}. The result contains an object.
	 */
	OBJECT_PARSE_RESULT,

	/**
	 * Result type of {@link ClassParseResult}. The result contains a class. This type is only internally relevant.
	 */
	CLASS_PARSE_RESULT,

	/**
	 * Result type of {@link ParseError}. The result describes an error message.
	 */
	PARSE_ERROR,

	/**
	 * Result type of {@link AmbiguousParseResult}. The result contains an error message describing the
	 * encountered ambiguity during parsing. This type is only internally relevant.
	 */
	AMBIGUOUS_PARSE_RESULT
}
