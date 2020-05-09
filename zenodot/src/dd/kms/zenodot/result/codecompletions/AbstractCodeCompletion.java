package dd.kms.zenodot.result.codecompletions;

import dd.kms.zenodot.matching.MatchRating;
import dd.kms.zenodot.result.CodeCompletion;
import dd.kms.zenodot.result.CodeCompletionType;
import dd.kms.zenodot.result.IntRange;
import dd.kms.zenodot.result.IntRanges;

import java.util.Objects;

abstract class AbstractCodeCompletion implements CodeCompletion
{
	private final CodeCompletionType	type;
	private final int					insertionBegin;
	private final int					insertionEnd;
	private final MatchRating			rating;

	AbstractCodeCompletion(CodeCompletionType type, int insertionBegin, int insertionEnd, MatchRating rating) {
		this.type = type;
		this.insertionBegin = insertionBegin;
		this.insertionEnd = insertionEnd;
		this.rating = rating;
	}

	@Override
	public CodeCompletionType getType() {
		return type;
	}

	@Override
	public final IntRange getInsertionRange() {
		return IntRanges.create(insertionBegin, insertionEnd);
	}

	@Override
	public MatchRating getRating() {
		return rating;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AbstractCodeCompletion that = (AbstractCodeCompletion) o;
		return insertionBegin == that.insertionBegin &&
			insertionEnd == that.insertionEnd &&
			type == that.type &&
			Objects.equals(rating, that.rating);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, insertionBegin, insertionEnd, rating);
	}
}
