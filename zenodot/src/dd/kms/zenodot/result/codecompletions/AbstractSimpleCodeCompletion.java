package dd.kms.zenodot.result.codecompletions;

import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.result.CodeCompletionType;

/**
 * Base class for all code completions that simply insert a string
 */
abstract class AbstractSimpleCodeCompletion extends AbstractCodeCompletion
{
	AbstractSimpleCodeCompletion(CodeCompletionType type, int insertionBegin, int insertionEnd, MatchRating rating) {
		super(type, insertionBegin, insertionEnd, rating);
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
