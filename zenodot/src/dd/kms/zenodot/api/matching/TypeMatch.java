package dd.kms.zenodot.api.matching;

/**
 * Different ratings for a type match.<br>
 * <br>
 * The lower the ordinal, the better the match.
 */
public enum TypeMatch
{
	/**
	 * The type equals the expected type.<br>
	 * <br>
	 * <b>Example:</b> {@code actual = int}, {@code expected = int}
	 */
	FULL,

	/**
	 * The type is a subtype of the expected type.<br>
	 * <br>
	 * <b>Example 1:</b> {@code actual = String}, {@code expected = Object}<br>
	 * <b>Example 2:</b> {@code actual = null type}, {@code expected = any reference type}
	 */
	INHERITANCE,

	/**
	 * The type and the expected type are both primitives and the type can be converted to the expected type without narrowing.<br>
	 * <br>
	 * <b>Example:</b> {@code actual = int}, {@code expected = double}
	 */
	WIDENING,

	/**
	 * The type can be unboxed and then possibly widened to the expected type, or
	 * it can be boxed and then up-casted to the expected type.<br>
	 * <br>
	 * <b>Example 1:</b> {@code actual = Integer}, {@code expected = int}<br>
	 * <b>Example 2:</b> {@code actual = Integer}, {@code expected = double}<br>
	 * <b>Example 3:</b> {@code actual = int}, {@code expected = Integer}
	 * <b>Example 4:</b> {@code actual = int}, {@code expected = Number}
	 */
	BOXED,

	/**
	 * The type does not match the expected type in any of the supported senses.
	 */
	NONE
}
