package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestion;
import dd.kms.zenodot.result.IntRange;
import dd.kms.zenodot.result.IntRanges;
import dd.kms.zenodot.settings.Variable;
import dd.kms.zenodot.utils.dataProviders.VariableDataProvider;

import java.util.Objects;

public class CompletionSuggestionVariable implements CompletionSuggestion
{
	private final Variable	variable;
	private final int 		insertionBegin;
	private final int 		insertionEnd;

	public CompletionSuggestionVariable(Variable variable, int insertionBegin, int insertionEnd) {
		this.variable = variable;
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
		return VariableDataProvider.getVariableDisplayText(variable);
	}

	@Override
	public String toString() {
		return VariableDataProvider.getVariableDisplayText(variable);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompletionSuggestionVariable that = (CompletionSuggestionVariable) o;
		return insertionBegin == that.insertionBegin &&
				insertionEnd == that.insertionEnd &&
				Objects.equals(variable, that.variable);
	}

	@Override
	public int hashCode() {
		return Objects.hash(variable, insertionBegin, insertionEnd);
	}
}
