package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestionType;
import dd.kms.zenodot.utils.wrappers.FieldInfo;

import java.util.Objects;

class CompletionSuggestionFieldImpl extends AbstractSimpleCompletionSuggestion
{
	private final FieldInfo	fieldInfo;

	CompletionSuggestionFieldImpl(FieldInfo fieldInfo, int insertionBegin, int insertionEnd) {
		super(CompletionSuggestionType.FIELD, insertionBegin, insertionEnd);
		this.fieldInfo = fieldInfo;
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
		CompletionSuggestionFieldImpl that = (CompletionSuggestionFieldImpl) o;
		return Objects.equals(fieldInfo, that.fieldInfo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), fieldInfo);
	}
}
