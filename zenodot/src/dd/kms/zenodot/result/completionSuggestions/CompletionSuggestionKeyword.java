package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.IntRange;
import dd.kms.zenodot.result.IntRanges;

import java.util.Objects;

public class CompletionSuggestionKeyword implements CompletionSuggestion
{
	private final String	keyword;
	private final int 		insertionBegin;
	private final int 		insertionEnd;

	public CompletionSuggestionKeyword(String keyword, int insertionBegin, int insertionEnd) {
		this.keyword = keyword;
		this.insertionBegin = insertionBegin;
		this.insertionEnd = insertionEnd;
	}

	@Override
	public IntRange getInsertionRange() {
		return IntRanges.create(insertionBegin, insertionEnd);
	}

	@Override
	public int getCaretPositionAfterInsertion() {
		return insertionBegin + getTextToInsert().length();
	}

	@Override
	public String getTextToInsert() {
		return keyword;
	}

	@Override
	public String toString() {
		return keyword;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompletionSuggestionKeyword that = (CompletionSuggestionKeyword) o;
		return insertionBegin == that.insertionBegin &&
			insertionEnd == that.insertionEnd &&
			Objects.equals(keyword, that.keyword);
	}

	@Override
	public int hashCode() {
		return Objects.hash(keyword, insertionBegin, insertionEnd);
	}
}
