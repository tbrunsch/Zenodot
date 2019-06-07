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
	 * Outcome type of {@link ObjectParseResult}. The result contains an object.
	 */
	OBJECT_PARSE_RESULT,

	/**
	 * Outcome type of {@link ClassParseResult}. The result contains a class.
	 */
	CLASS_PARSE_RESULT,

	/**
	 * Outcome type of {@link PackageParseResult}. The result contains a package.
	 */
	PACKAGE_PARSE_RESULT,

	/**
	 * Outcome type of {@link ParseError}. The result describes an error message.
	 */
	PARSE_ERROR,

	/**
	 * Outcome type of {@link AmbiguousParseResult}. The result contains an error message describing the
	 * encountered ambiguity during parsing. This type is only internally relevant.
	 */
	AMBIGUOUS_PARSE_RESULT
}
