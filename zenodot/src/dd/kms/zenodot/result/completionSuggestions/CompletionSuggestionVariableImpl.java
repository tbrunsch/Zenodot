package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestionType;
import dd.kms.zenodot.settings.Variable;

import java.util.Objects;

class CompletionSuggestionVariableImpl extends AbstractSimpleCompletionSuggestion implements CompletionSuggestionVariable
{
	private final Variable	variable;

	CompletionSuggestionVariableImpl(Variable variable, int insertionBegin, int insertionEnd) {
		super(CompletionSuggestionType.VARIABLE, insertionBegin, insertionEnd);
		this.variable = variable;
	}

	@Override
	public Variable getVariable() {
		return variable;
	}

	@Override
	public String toString() {
		return variable.getName();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		CompletionSuggestionVariableImpl that = (CompletionSuggestionVariableImpl) o;
		return Objects.equals(variable, that.variable);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), variable);
	}
}
