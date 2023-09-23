package dd.kms.zenodot.api.result;

import dd.kms.zenodot.api.matching.MatchRating;

/**
 * Description of one code completion
 */
public interface CodeCompletion
{
	/**
	 * Returns the type of the completion
	 */
	CodeCompletionType getType();

	/**
	 * Returns the first index of the text (inclusive) that should be replaced by the code completion.
	 */
	int getInsertionBegin();

	/**
	 * Returns the last index of the text (exclusive) that should be replaced by the code completion.
	 */
	int getInsertionEnd();

	/**
	 * Returns the suggested caret position after inserting the code completions
	 */
	int getCaretPositionAfterInsertion();

	/**
	 * Returns the code completion text
	 */
	String getTextToInsert();

	/**
	 * Returns a String representation for displaying the code completion.
	 */
	String toString();

	/**
	 * Returns a rating how good the code completion matches the typed text
	 */
	MatchRating getRating();
}
