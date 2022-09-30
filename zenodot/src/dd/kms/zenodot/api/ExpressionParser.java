package dd.kms.zenodot.api;

import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;

import java.util.List;
import java.util.Optional;

/**
 * Parser for expressions<br/>
 * <br/>
 * Zenodot provided code completions and evaluates expressions in the context of a certain object referred to by {@code this}.
 * If the context refers to, e.g., a list, then you can simply type {@code size()} to get the size of that list. You do
 * not have to enter {@code this.size()}.
 */
public interface ExpressionParser
{
	/**
	 * Returns rated code completions for the given text at a given caret position in the context provided by {@code thisValue}.
	 */
	List<CodeCompletion> getCompletions(String text, int caretPosition, ObjectInfo thisValue) throws ParseException;

	/**
	 * Returns optional information about the arguments of the current method or constructor {@link ExecutableArgumentInfo}.
	 * The value will be present if the caret is inside of a method argument list.
	 */
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(String expression, int caretPosition, ObjectInfo thisValue) throws ParseException;

	/**
	 * Evaluates the expression in the context provided by {@code thisValue}.
	 */
	 ObjectInfo evaluate(String expression, ObjectInfo thisValue) throws ParseException;

	/**
	 * Compiles the expression in the context provided by {@code thisValue}.<br/>
	 * <br/>
	 * This method requires information about an object instead of a class. The reason
	 * is that with {@link dd.kms.zenodot.api.settings.EvaluationMode#DYNAMIC_TYPING} or
	 * {@link dd.kms.zenodot.api.settings.EvaluationMode#MIXED} also runtime type information
	 * will be considered. If you want to compile an expression based on a class, then you
	 * can call {@link InfoProvider#createObjectInfo(Object, Class)} for the object
	 * {@link InfoProvider#INDETERMINATE_VALUE} and the class and use this as {@code thisValue}.
	 */
	CompiledExpression compile(String expression, ObjectInfo thisValue) throws ParseException;
}
