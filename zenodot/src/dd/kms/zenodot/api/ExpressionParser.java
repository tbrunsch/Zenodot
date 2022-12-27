package dd.kms.zenodot.api;

import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;

import java.util.List;
import java.util.Optional;

/**
 * Parser for expressions<br>
 * <br>
 * Zenodot provides code completions and evaluates expressions in the context of a certain object referred to by {@code this}.
 * If the context refers to, e.g., a list, then you can simply type {@code size()} to get the size of that list. You do
 * not have to enter {@code this.size()}.
 */
public interface ExpressionParser
{
	/**
	 * Returns rated code completions for the given text at a given caret position in the context provided by {@code thisType}.
	 */
	List<CodeCompletion> getCompletions(String text, int caretPosition, Class<?> thisType) throws ParseException;

	/**
	 * Returns rated code completions for the given text at a given caret position in the context provided by {@code thisValue}.
	 */
	List<CodeCompletion> getCompletions(String text, int caretPosition, Object thisValue) throws ParseException;

	/**
	 * Returns optional information about the arguments of the current method or constructor {@link ExecutableArgumentInfo}.
	 * The value will be present if the caret is inside of a method argument list.
	 */
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String expression, int caretPosition, Class<?> thisType) throws ParseException;

	/**
	 * Returns optional information about the arguments of the current method or constructor {@link ExecutableArgumentInfo}.
	 * The value will be present if the caret is inside of a method argument list.
	 */
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String expression, int caretPosition, Object thisValue) throws ParseException;

	/**
	 * Evaluates the expression in the context provided by {@code thisValue}.
	 */
	 Object evaluate(String expression, Object thisValue) throws ParseException;

	/**
	 * Compiles the expression in the context provided by {@code thisType}.
	 */
	CompiledExpression compile(String expression, Class<?> thisType) throws ParseException;

	/**
	 * Compiles the expression in the context provided by {@code thisValue}.<br>
	 * <br>
	 * This method requires information about an object instead of a class. The reason
	 * is that with {@link dd.kms.zenodot.api.settings.EvaluationMode#DYNAMIC_TYPING} or
	 * {@link dd.kms.zenodot.api.settings.EvaluationMode#MIXED} also runtime type information
	 * will be considered. If you want to compile an expression based on a class, then you
	 * can call {@link #compile(String, Class)} instead.
	 */
	CompiledExpression compile(String expression, Object thisValue) throws ParseException;
}
