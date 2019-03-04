package dd.kms.zenodot.matching;

import dd.kms.zenodot.common.RegexUtils;

/**
 * Different ratings for a string match. Used for rating name matches.<br/>
 * <br/>
 * The lower the ordinal, the better the match.
 */
public enum StringMatch
{
	/**
	 * The string equals the expected string.<br/>
	 * <br/>
	 * <b>Example:</b> {@code actual = "match"}, {@code expected = "match"}
	 */
	FULL,

	/**
	 * The string equals the expected string when ignoring case-sensitivity.<br/>
	 * <br/>
	 * <b>Example:</b> {@code actual = "match"}, {@code expected = "Match"}
	 */
	FULL_IGNORE_CASE,

	/**
	 * The string starts with the expected string.<br/>
	 * <br/>
	 * <b>Example:</b> {@code actual = "matchbox"}, {@code expected = "match"}
	 */
	PREFIX,

	/**
	 * The string starts with the expected string when ignoring case-sensitivity.<br/>
	 * <br/>
	 * <b>Example:</b> {@code actual = "matchbox"}, {@code expected = "Match"}
	 */
	PREFIX_IGNORE_CASE,

	/**
	 * The expected string starts with the string.<br/>
	 * <br/>
	 * <b>Example:</b> {@code actual = "match"}, {@code expected = "matchbox"}
	 */
	INVERSE_PREFIX,

	/**
	 * The expected string starts with the string when ignoring case-sensitivity.<br/>
	 * <br/>
	 * <b>Example:</b> {@code actual = "Match"}, {@code expected = "matchbox"}
	 */
	INVERSE_PREFIX_IGNORE_CASE,

	/**
	 * The string matches the expected string when interpreting the expected string as wildcard string (see {@link RegexUtils#createRegexForWildcardString(String)}).<br/>
	 * <br/>
	 * <b>Example:</b> {@code actual = "myCustomSettings"}, {@code expected = "*CS"}
	 */
	WILDCARD,

	/**
	 * The string does not match the expected string in any of the currently supported senses.
	 */
	NONE
}
