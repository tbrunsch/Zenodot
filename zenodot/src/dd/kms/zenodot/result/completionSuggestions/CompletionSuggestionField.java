package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.IntRange;
import dd.kms.zenodot.result.IntRanges;
import dd.kms.zenodot.utils.dataProviders.FieldDataProvider;
import dd.kms.zenodot.utils.wrappers.FieldInfo;

import java.util.Objects;

public class CompletionSuggestionField implements CompletionSuggestion
{
	private final FieldInfo	fieldInfo;
	private final int 		insertionBegin;
	private final int 		insertionEnd;

	public CompletionSuggestionField(FieldInfo fieldInfo, int insertionBegin, int insertionEnd) {
		this.fieldInfo = fieldInfo;
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
		return fieldInfo.getName();
	}

	@Override
	public String toString() {
		return FieldDataProvider.getFieldDisplayText(fieldInfo);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompletionSuggestionField that = (CompletionSuggestionField) o;
		return insertionBegin == that.insertionBegin &&
				insertionEnd == that.insertionEnd &&
				Objects.equals(fieldInfo, that.fieldInfo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fieldInfo, insertionBegin, insertionEnd);
	}
}
