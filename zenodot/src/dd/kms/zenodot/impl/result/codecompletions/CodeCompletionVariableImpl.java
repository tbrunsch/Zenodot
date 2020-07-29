package dd.kms.zenodot.impl.result.codecompletions;

import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionVariable;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.settings.Variable;

import java.util.Objects;

class CodeCompletionVariableImpl extends AbstractSimpleCodeCompletion implements CodeCompletionVariable
{
	private final Variable	variable;

	CodeCompletionVariableImpl(Variable variable, int insertionBegin, int insertionEnd, MatchRating rating) {
		super(CodeCompletionType.VARIABLE, insertionBegin, insertionEnd, rating);
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
		CodeCompletionVariableImpl that = (CodeCompletionVariableImpl) o;
		return Objects.equals(variable, that.variable);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), variable);
	}
}
