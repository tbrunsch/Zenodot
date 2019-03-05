package dd.kms.zenodot.utils;

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
	WITHOUT_EVALUATION
}
