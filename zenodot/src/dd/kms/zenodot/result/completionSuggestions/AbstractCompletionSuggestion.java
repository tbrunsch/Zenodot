package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.CompletionSuggestionType;
import dd.kms.zenodot.result.IntRange;
import dd.kms.zenodot.result.IntRanges;

import java.util.Objects;

abstract class AbstractCompletionSuggestion  implements CompletionSuggestion
{
	private final CompletionSuggestionType	type;
	private final int						insertionBegin;
	private final int						insertionEnd;

	AbstractCompletionSuggestion(CompletionSuggestionType type, int insertionBegin, int insertionEnd) {
		this.type = type;
		this.insertionBegin = insertionBegin;
		this.insertionEnd = insertionEnd;
	}

	@Override
	public CompletionSuggestionType getType() {
		return type;
	}

	@Override
	public final IntRange getInsertionRange() {
		return IntRanges.create(insertionBegin, insertionEnd);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AbstractCompletionSuggestion that = (AbstractCompletionSuggestion) o;
		return insertionBegin == that.insertionBegin &&
			insertionEnd == that.insertionEnd;
	}

	@Override
	public int hashCode() {
		return Objects.hash(insertionBegin, insertionEnd);
	}
}
