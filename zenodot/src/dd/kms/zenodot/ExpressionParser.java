package dd.kms.zenodot;

import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.utils.wrappers.ObjectInfo;

import java.util.Map;
import java.util.Optional;

/**
 * Parser for expressions<br/>
 * <br/>
 * Zenodot suggests completions and evaluates expressions in the context of a certain object referred to by {@code this}.
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
	Map<CompletionSuggestion, MatchRating> suggestCodeCompletion(int caretPosition) throws ParseException;

	/**
	 * Returns optional information about the arguments of the current method or constructor {@link ExecutableArgumentInfo}.
	 * The value will be present if the caret is inside of a method argument list.
	 */
	Optional<ExecutableArgumentInfo> getExecutableArgumentInfo(int caretPosition) throws ParseException;

	/**
	 * Evaluates the expression in the context provided by {@code thisValue}.
	 *
	 * @throws ParseException
	 */
	 ObjectInfo evaluate() throws ParseException;
}
