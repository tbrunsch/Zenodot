package dd.kms.zenodot.result;

public interface CompletionSuggestionIF
{
	IntRange getInsertionRange();
	int getCaretPositionAfterInsertion();
	String getTextToInsert();
	String toString();
}
