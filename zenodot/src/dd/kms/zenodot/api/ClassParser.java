package dd.kms.zenodot.api;

import dd.kms.zenodot.api.result.CodeCompletion;

import java.util.List;

/**
 * Parser for class names
 */
public interface ClassParser
{
	/**
	 * Returns rated code completions for the given text at a given caret position taking the imports specified in the settings into account
	 *
	 * @throws ParseException
	 */
	List<CodeCompletion> getCompletions(String text, int caretPosition) throws ParseException;

	/**
	 * Evaluates the specified class name as a class taking the imports specified in the settings into account
	 *
	 * @throws ParseException
	 */
	 Class<?> evaluate(String className) throws ParseException;
}
