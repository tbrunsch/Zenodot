package dd.kms.zenodot.impl.result.codecompletions;

import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionField;
import dd.kms.zenodot.api.wrappers.FieldInfo;

import java.util.Objects;

class CodeCompletionFieldImpl extends AbstractSimpleCodeCompletion implements CodeCompletionField
{
	private final FieldInfo	fieldInfo;

	CodeCompletionFieldImpl(FieldInfo fieldInfo, int insertionBegin, int insertionEnd, MatchRating rating) {
		super(CodeCompletionType.FIELD, insertionBegin, insertionEnd, rating);
		this.fieldInfo = fieldInfo;
	}

	@Override
	public FieldInfo getFieldInfo() {
		return fieldInfo;
	}

	@Override
	public String toString() {
		return fieldInfo.getName();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		CodeCompletionFieldImpl that = (CodeCompletionFieldImpl) o;
		return Objects.equals(fieldInfo, that.fieldInfo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), fieldInfo);
	}
}
