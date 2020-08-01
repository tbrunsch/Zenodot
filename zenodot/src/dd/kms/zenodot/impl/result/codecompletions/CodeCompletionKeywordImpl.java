package dd.kms.zenodot.impl.result.codecompletions;

import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionKeyword;

import java.util.Objects;

class CodeCompletionKeywordImpl extends AbstractSimpleCodeCompletion implements CodeCompletionKeyword
{
	private final String	keyword;

	CodeCompletionKeywordImpl(String keyword, int insertionBegin, int insertionEnd, MatchRating rating) {
		super(CodeCompletionType.KEYWORD, insertionBegin, insertionEnd, rating);
		this.keyword = keyword;
	}

	@Override
	public String getKeyword() {
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
		if (!super.equals(o)) return false;
		CodeCompletionKeywordImpl that = (CodeCompletionKeywordImpl) o;
		return Objects.equals(keyword, that.keyword);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), keyword);
	}
}
