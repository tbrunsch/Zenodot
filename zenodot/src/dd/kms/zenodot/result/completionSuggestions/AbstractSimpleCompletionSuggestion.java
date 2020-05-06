package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestionType;

/**
 * Base class for all completions suggestions that simply insert a string
 */
abstract class AbstractSimpleCompletionSuggestion extends AbstractCompletionSuggestion
{
	AbstractSimpleCompletionSuggestion(CompletionSuggestionType type, int insertionBegin, int insertionEnd) {
		super(type, insertionBegin, insertionEnd);
	}

	@Override
	public abstract String toString();

	@Override
	public final int getCaretPositionAfterInsertion() {
		return getInsertionRange().getBegin() + getTextToInsert().length();
	}

	@Override
	public String getTextToInsert() {
		return toString();
	}
}
