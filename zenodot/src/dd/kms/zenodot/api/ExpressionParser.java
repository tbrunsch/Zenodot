package dd.kms.zenodot.api;

import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.ExecutableArgumentInfo;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.api.wrappers.InfoProvider;
import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;

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
	 * Returns rated code completions for the expression at a given caret position in the context provided by {@code thisValue}.
	 *
	 * @throws ParseException
	 */
	List<CodeCompletion> getCompletions(ObjectInfo thisValue, int caretPosition) throws ParseException;

	/**
	 * Returns optional information about the arguments of the current method or constructor {@link ExecutableArgumentInfo}.
	 * The value will be present if the caret is inside of a method argument list.
	 */
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(ObjectInfo thisValue, int caretPosition) throws ParseException;

	/**
	 * Evaluates the expression in the context provided by {@code thisValue}.
	 *
	 * @throws ParseException
	 */
	 ObjectInfo evaluate(ObjectInfo thisValue) throws ParseException;

	/**
	 * Compiles the expression in the context provided by {@code thisValue}.<br/>
	 * <br/>
	 * If dynamic typing is disabled (see {@link ParserSettings#isEnableDynamicTyping()}),
	 * then a {@link TypeInfo} instead of an {@link ObjectInfo} would suffice as well.
	 * To create an {@code ObjectInfo} from a {@link TypeInfo}, you can use the method
	 * {@link InfoProvider#createObjectInfo(Object, TypeInfo)} for the object
	 * {@link InfoProvider#INDETERMINATE_VALUE}.
	 *
	 * @throws ParseException
	 */
	CompiledExpression compile(ObjectInfo thisValue) throws ParseException;
}
