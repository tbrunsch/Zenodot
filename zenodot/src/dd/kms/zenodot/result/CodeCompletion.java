package dd.kms.zenodot.result;

import dd.kms.zenodot.matching.MatchRating;

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
	 * Returns the range that should be replaced by the code completions
	 */
	IntRange getInsertionRange();

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
