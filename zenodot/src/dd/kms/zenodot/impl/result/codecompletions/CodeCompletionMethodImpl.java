package dd.kms.zenodot.impl.result.codecompletions;

import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionMethod;
import dd.kms.zenodot.api.wrappers.ExecutableInfo;
import dd.kms.zenodot.impl.utils.dataproviders.ExecutableDataProvider;

import java.util.Objects;

class CodeCompletionMethodImpl extends AbstractCodeCompletion implements CodeCompletionMethod
{
	private final ExecutableInfo	methodInfo;

	CodeCompletionMethodImpl(ExecutableInfo methodInfo, int insertionBegin, int insertionEnd, MatchRating rating) {
		super(CodeCompletionType.METHOD, insertionBegin, insertionEnd, rating);
		this.methodInfo = methodInfo;
	}

	@Override
	public ExecutableInfo getMethodInfo() {
		return methodInfo;
	}

	@Override
	public int getCaretPositionAfterInsertion() {
		boolean hasArguments = methodInfo.getNumberOfArguments() > 0;
		int insertionBegin = getInsertionRange().getBegin();
		return insertionBegin + (hasArguments
				? methodInfo.getName().length() + 1
				: getTextToInsert().length());
	}

	@Override
	public String getTextToInsert() {
		return methodInfo.getName() + "()";
	}

	@Override
	public String toString() {
		return ExecutableDataProvider.getMethodDisplayText(methodInfo);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		CodeCompletionMethodImpl that = (CodeCompletionMethodImpl) o;
		return Objects.equals(methodInfo, that.methodInfo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), methodInfo);
	}
}
