package dd.kms.zenodot;

import dd.kms.zenodot.matching.StringMatch;
import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.utils.wrappers.ClassInfo;
import dd.kms.zenodot.utils.wrappers.PackageInfo;

import java.util.Map;

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
	Map<CompletionSuggestion, StringMatch> suggestCodeCompletion(int caretPosition) throws ParseException;

	/**
	 * Evaluates the specified package name. The result should be the same as {@link Package#getPackage(String)},
	 * but it is achieved with internal Zenodot logic and is, hence, consistent with the code completion of
	 * packages.
	 *
	 * @throws ParseException
	 */
	 PackageInfo evaluate() throws ParseException;
}
