package dd.kms.zenodot;

import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.utils.wrappers.PackageInfo;

import java.util.List;

/**
 * Parser for package names
 */
public interface PackageParser
{
	/**
	 * Returns rated code completions for the text at a given caret position
	 *
	 * @throws ParseException
	 */
	List<CodeCompletion> getCompletions(int caretPosition) throws ParseException;

	/**
	 * Evaluates the specified package name. The result should be the same as {@link Package#getPackage(String)},
	 * but it is achieved with internal Zenodot logic and is, hence, consistent with the code completion of
	 * packages.
	 *
	 * @throws ParseException
	 */
	 PackageInfo evaluate() throws ParseException;
}
