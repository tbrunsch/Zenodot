package dd.kms.zenodot.api;

import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.wrappers.ClassInfo;

import java.util.List;

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
	List<CodeCompletion> getCompletions(int caretPosition) throws ParseException;

	/**
	 * Evaluates the specified class name as a class taking the imports specified in the settings into account
	 *
	 * @throws ParseException
	 */
	 ClassInfo evaluate() throws ParseException;
}
