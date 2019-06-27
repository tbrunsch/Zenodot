package dd.kms.zenodot;

import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;

import java.util.Map;
import java.util.Optional;

/**
 * Compiler for expressions<br/>
 * <br/>
 * In contrast to the {@link ExpressionParser}, the compiler returns a {@link CompiledExpression}
 * instead of an object. This parse result allows a faster evaluation of the same expression for different
 * values of {@code this} compared to evaluating the expression for each of these values individually.
 */
public interface ExpressionCompiler
{
	/**
	 * Returns rated code completions for the expression at a given caret position in the context provided by {@code thisClass}.
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
	 * Compiles the expression in the context provided by {@code thisClass}.
	 *
	 * @throws ParseException
	 */
	CompiledExpression compile() throws ParseException;
}
