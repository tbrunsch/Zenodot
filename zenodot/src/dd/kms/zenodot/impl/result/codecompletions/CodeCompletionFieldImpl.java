package dd.kms.zenodot.impl.result.codecompletions;

import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionField;

import java.lang.reflect.Field;
import java.util.Objects;

class CodeCompletionFieldImpl extends AbstractSimpleCodeCompletion implements CodeCompletionField
{
	private final Field	field;

	CodeCompletionFieldImpl(Field field, int insertionBegin, int insertionEnd, MatchRating rating) {
		super(CodeCompletionType.FIELD, insertionBegin, insertionEnd, rating);
		this.field = field;
	}

	@Override
	public Field getField() {
		return field;
	}

	@Override
	public String toString() {
		return field.getName();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		CodeCompletionFieldImpl that = (CodeCompletionFieldImpl) o;
		return Objects.equals(field, that.field);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), field);
	}
}
