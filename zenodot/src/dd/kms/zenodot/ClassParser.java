package dd.kms.zenodot;

import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.utils.wrappers.ClassInfo;

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
