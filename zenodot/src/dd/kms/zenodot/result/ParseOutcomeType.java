package dd.kms.zenodot.result;

/**
 * Describes the type of a concrete {@link ParseOutcome}
 */
public enum ParseOutcomeType
{
	/**
	 * Outcome type of {@link CodeCompletions}. The result contains code completions.
	 */
	CODE_COMPLETIONS,

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
