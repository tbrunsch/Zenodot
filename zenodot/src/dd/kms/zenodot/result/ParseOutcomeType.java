package dd.kms.zenodot.result;

/**
 * Describes the type of a concrete {@link ParseOutcome}
 */
public enum ParseOutcomeType
{
	/**
	 * Outcome type of {@link CompletionSuggestions}. The result contains completion suggestions.
	 */
	COMPLETION_SUGGESTIONS,

	/**
	 * Outcome type of {@link ParseResult}.
	 */
	RESULT,

	/**
	 * Outcome type of {@link ParseError}. The result describes an error message.
	 */
	ERROR,

	/**
	 * Outcome type of {@link AmbiguousParseResult}. The result contains an error message describing the
	 * encountered ambiguity during parsing. This type is only internally relevant.
	 */
	AMBIGUOUS_RESULT
}
