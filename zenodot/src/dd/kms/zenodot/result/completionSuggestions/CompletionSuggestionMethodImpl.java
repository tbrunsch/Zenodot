package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestionType;
import dd.kms.zenodot.utils.dataProviders.ExecutableDataProvider;
import dd.kms.zenodot.utils.wrappers.ExecutableInfo;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class CompletionSuggestionMethodImpl extends AbstractCompletionSuggestion implements CompletionSuggestionMethod
{
	private final ExecutableInfo	methodInfo;

	CompletionSuggestionMethodImpl(ExecutableInfo methodInfo, int insertionBegin, int insertionEnd) {
		super(CompletionSuggestionType.METHOD, insertionBegin, insertionEnd);
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
		CompletionSuggestionMethodImpl that = (CompletionSuggestionMethodImpl) o;
		return Objects.equals(methodInfo, that.methodInfo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), methodInfo);
	}
}
