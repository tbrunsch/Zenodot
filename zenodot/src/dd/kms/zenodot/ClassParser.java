package dd.kms.zenodot;

import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.ExecutableArgumentInfo;
import dd.kms.zenodot.utils.wrappers.ClassInfo;

import java.util.Map;
import java.util.Optional;

/**
 * Parser for class names
 */
public interface ClassParser
{
	/**
	 * Returns rated code completions for the text at a given caret position taking the imports specified in the settings into account
	 *
	 * @throws ParseException
	 */
	Map<CompletionSuggestion, StringMatch> suggestCodeCompletion(int caretPosition) throws ParseException;

	/**
	 * Evaluates the specified class name as a class taking the imports specified in the settings into account
	 *
	 * @throws ParseException
	 */
	 ClassInfo evaluate() throws ParseException;
}
