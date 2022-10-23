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
	 * <b>Example:</b> {@code actual = Integer}, {@code expected = Number}
	 */
	INHERITANCE,

	/**
	 * The type and the expected type are both primitives and the type can be converted to the expected type without narrowing.<br>
	 * <br>
	 * <b>Example:</b> {@code actual = int}, {@code expected = double}
	 */
	PRIMITIVE_CONVERSION,

	/**
	 * The type is the boxed (wrapper) type of the expected type or vice versa.<br>
	 * <br>
	 * <b>Example 1:</b> {@code actual = Integer}, {@code expected = int}<br>
	 * <b>Example 2:</b> {@code actual = int}, {@code expected = Integer}
	 */
	BOXED,

	/**
	 * Either the type can be unboxed and then converted to the expected type or it can be converted and then boxed to the
	 * expected type. In any case, narrowing is not allowed. <br>
	 * <br>
	 * <b>Example 1:</b> {@code actual = Integer}, {@code expected = double}<br>
	 * <b>Example 2:</b> {@code actual = int}, {@code expected = Double}
	 */
	BOXED_AND_CONVERSION,

	/**
	 * The type is primitive and its boxed class is a subtype of the expected type.<br>
	 * <br>
	 * <b>Example:</b> {@code actual = int}, {@code expected = Number}
	 */
	BOXED_AND_INHERITANCE,

	/**
	 * The type does not match the expected type in any of the supported senses.
	 */
	NONE
}
