package dd.kms.zenodot.utils;

import dd.kms.zenodot.result.CompiledObjectParseResult;
import dd.kms.zenodot.settings.ParserSettings;

/**
 * Internal mode to describe how expressions should be parsed. Together with the flag
 * {@link ParserSettings#isEnableDynamicTyping()} it specifies which evaluation mode
 * will be used when evaluating expressions.
 */
public enum ParseMode
{
	/**
	 * Used for code completion. Expressions are only evaluated if dynamic typing is activated.
	 */
	CODE_COMPLETION,

	/**
	 * Used for regular expression evaluation. Evaluation is either based on declared types (static typing)
	 * or on runtime types (dynamic typing).
	 */
	EVALUATION,

	/**
	 * Internal parse mode that suppresses evaluation to avoid side effects.
	 */
	WITHOUT_EVALUATION,

	/**
	 * Uses for expression compilation. In contrast to {@link #EVALUATION}, the expression is not evaluated
	 * to obtain an object, but it is compiled into an {@link CompiledObjectParseResult}.
	 * This compiled result is parameterized in {@code this} and can be evaluated by plugging in certain
	 * values for {@code this}. For multiple evaluations this is usually faster than evaluating the expression
	 * for each value of {@code this}.
	 */
	COMPILATION
}
