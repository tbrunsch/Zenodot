package dd.kms.zenodot.impl.result.codecompletions;

import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionVariable;
import dd.kms.zenodot.framework.result.codecompletions.AbstractSimpleCodeCompletion;

import java.util.Objects;

class CodeCompletionVariableImpl extends AbstractSimpleCodeCompletion implements CodeCompletionVariable
{
	private final String variableName;

	CodeCompletionVariableImpl(String variableName, int insertionBegin, int insertionEnd, MatchRating rating) {
		super(CodeCompletionType.VARIABLE, insertionBegin, insertionEnd, rating);
		this.variableName = variableName;
	}

	@Override
	public String getVariableName() {
		return variableName;
	}

	@Override
	public String toString() {
		return variableName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		CodeCompletionVariableImpl that = (CodeCompletionVariableImpl) o;
		return Objects.equals(variableName, that.variableName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), variableName);
	}
}
