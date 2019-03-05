package dd.kms.zenodot.utils;

/**
 * Mode that describes how expressions are evaluated. This mode cannot be set explicitly.
 */
public enum EvaluationMode
{
	/**
	 * Do not evaluate expressions at all<br/>
	 * <br/>
	 * This is the usual mode for code completion.
	 */
	NONE,

	/**
	 * Use declared types<br/>
	 * <br/>
	 * This is the usual mode when evaluating expressions.
	 */
	STATICALLY_TYPED,

	/**
	 * Use runtime types<br/>
	 * <br/>
	 * Sometimes, especially when analyzing the internal state of objects, you do not care
	 * about declared types because you are interested in type-specific details.
	 * In such cases, dynamic typing can avoid unnecessary casts.<br/>
	 * <br/>
	 * <b>Example:</b> Consider the declaration {@code Object o = "12345";} and assume that
	 *                 you want to know the length of that {@code String}.
	 *                 With static typing, you have to write {@code ((String) o).length()}.
	 *                 With dynamic typing, you can write {@code o.length()}.<br/>
	 * <br/>
	 * There are two pitfalls you should be aware of:
	 * <ul>
	 *     <li>
	 *			With dynamic typing, methods will be executed to determine the runtime type
	 *			of the return value. If you use dynamic typing for code completion, then this
	 *			might cause unexpected side effects.
	 *     </li>
	 *     <li>
	 *			Method overload resolution can yield a different result with dynamic typing.<br/>
	 *			<br/>
	 *			<b>Example:</b> Consider the declaration {@code Object o = "12345";} and the two overloaded methods<br/>
	 *			<br/>
	 *			{@code String getType(Object o) { return "object"; }}<br/>
	 *			{@code String getType(String s) { return "string"; }}<br/>
	 *			<br/>
	 *			With static typing, the call {@code getType(o)} will return "object".
	 *			With dynamic typing, the call {@code getType(o)} will return "string".
	 *     </li>
	 * </ul>
	 */
	DYNAMICALLY_TYPED
}
