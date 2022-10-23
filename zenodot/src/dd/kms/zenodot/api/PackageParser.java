package dd.kms.zenodot.api;

import dd.kms.zenodot.api.result.CodeCompletion;

import java.util.List;

/**
 * Parser for package names
 */
public interface PackageParser
{
	/**
	 * Returns rated code completions for the given text at a given caret position
	 *
	 * @throws ParseException
	 */
	List<CodeCompletion> getCompletions(String text, int caretPosition) throws ParseException;

	/**
	 * Evaluates the specified package name. The result should be the same as {@link Package#getPackage(String)},
	 * but it is achieved with internal Zenodot logic and is, hence, consistent with the code completion of
	 * packages.
	 *
	 * @throws ParseException
	 */
	 String evaluate(String packageName) throws ParseException;
}
