package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestionType;

import java.util.Objects;

class CompletionSuggestionKeywordImpl extends AbstractSimpleCompletionSuggestion
{
	private final String	keyword;

	CompletionSuggestionKeywordImpl(String keyword, int insertionBegin, int insertionEnd) {
		super(CompletionSuggestionType.KEYWORD, insertionBegin, insertionEnd);
		this.keyword = keyword;
	}

	@Override
	public String toString() {
		return keyword;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		CompletionSuggestionKeywordImpl that = (CompletionSuggestionKeywordImpl) o;
		return Objects.equals(keyword, that.keyword);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), keyword);
	}
}
