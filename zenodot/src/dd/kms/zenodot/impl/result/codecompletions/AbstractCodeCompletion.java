package dd.kms.zenodot.impl.result.codecompletions;

import com.google.common.collect.Range;
import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.result.CodeCompletion;
import dd.kms.zenodot.api.result.CodeCompletionType;

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
	public int getInsertionBegin() {
		return insertionBegin;
	}

	@Override
	public int getInsertionEnd() {
		return insertionEnd;
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
