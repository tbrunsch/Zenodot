package dd.kms.zenodot.framework.result.codecompletions;

import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.result.CodeCompletionType;

/**
 * Base class for all code completions that simply insert a string
 */
public abstract class AbstractSimpleCodeCompletion extends AbstractCodeCompletion
{
	protected AbstractSimpleCodeCompletion(CodeCompletionType type, int insertionBegin, int insertionEnd, MatchRating rating) {
		super(type, insertionBegin, insertionEnd, rating);
	}

	@Override
	public abstract String toString();

	@Override
	public final int getCaretPositionAfterInsertion() {
		return getInsertionBegin() + getTextToInsert().length();
	}

	@Override
	public String getTextToInsert() {
		return toString();
	}
}
