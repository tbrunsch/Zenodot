package dd.kms.zenodot.api.settings;

public enum EvaluationMode
{
	/**
	 * Expressions will be evaluated based on the declared types. When accessing a field of type {@code Object} that is
	 * assigned a {@code String}, then only methods and fields of {@code Object} can be accessed. The runtime type is
	 * ignored.
	 */
	STATIC_TYPING,

	/**
	 * Expressions will be evaluated based on the runtime types. When accessing a field of type {@code Object} that is
	 * assigned a {@code String}, then all methods and fields of {@code String} can be accessed. Note, however, that
	 * in this mode methods and assignments will also be evaluated in the course of evaluating an expression even if it
	 * finally turns out that the expression cannot be parsed, as well as during code completion. This can cause side
	 * effects. A compromise between {@link #STATIC_TYPING} and {@code DYNAMIC_TYPING} that avoid these side effects is
	 * {@link #MIXED}.
	 */
	DYNAMIC_TYPING,

	/**
	 * Hybrid between {@link #STATIC_TYPING} and {@link #DYNAMIC_TYPING}:
	 * <ul>
	 *     <li>
	 *         As with {@code STATIC_TYPING}, methods and assignments are not executed during code completion and when
	 *         the expression cannot be parsed correctly. This avoids side effects until the final evaluation of the
	 *         expression, but this also means that only the declared return type of a method can be considered.
	 *     </li>
	 *     <li>
	 *         As with {@code DYNAMIC_TYPING}, all other syntactic elements (e.g., field access or array element access)
	 *         are evaluated based on their runtime types instead of their declared types, which saves casts during
	 *         code completion and evaluation.
	 *     </li>
	 * </ul>
	 */
	MIXED
}
