package dd.kms.zenodot.result;

/**
 * Description of one completion suggestion.
 */
public interface CompletionSuggestion
{
	/**
	 * Returns the range that should be replaced by the completion suggestion
	 */
	IntRange getInsertionRange();

	/**
	 * Returns the suggested caret position after inserting the completion suggestion
	 */
	int getCaretPositionAfterInsertion();

	/**
	 * Returns the completion suggestion
	 */
	String getTextToInsert();

	/**
	 * Return a String representation for displaying the completion suggestion.
	 */
	String toString();
}
