package dd.kms.zenodot.result.completionSuggestions;

import dd.kms.zenodot.result.CompletionSuggestionIF;
import dd.kms.zenodot.result.IntRange;
import dd.kms.zenodot.utils.dataProviders.ExecutableDataProvider;
import dd.kms.zenodot.utils.wrappers.AbstractExecutableInfo;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CompletionSuggestionMethod implements CompletionSuggestionIF
{
	private final AbstractExecutableInfo methodInfo;
	private final int	 			insertionBegin;
	private final int 				insertionEnd;

	public CompletionSuggestionMethod(AbstractExecutableInfo methodInfo, int insertionBegin, int insertionEnd) {
		this.methodInfo = methodInfo;
		this.insertionBegin = insertionBegin;
		this.insertionEnd = insertionEnd;
	}

	@Override
	public IntRange getInsertionRange() {
		return new IntRange(insertionBegin, insertionEnd);
	}

	@Override
	public int getCaretPositionAfterInsertion() {
		return insertionBegin + methodInfo.getName().length() + (methodInfo.getNumberOfArguments() == 0 ? 2 : 1);
	}

	@Override
	public String getTextToInsert() {
		return methodInfo.getName()
				+ "("
				+ IntStream.range(0, methodInfo.getNumberOfArguments()).mapToObj(param -> "").collect(Collectors.joining(", "))
				+ ")";
	}

	@Override
	public String toString() {
		return ExecutableDataProvider.getMethodDisplayText(methodInfo);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompletionSuggestionMethod that = (CompletionSuggestionMethod) o;
		return insertionBegin == that.insertionBegin &&
				insertionEnd == that.insertionEnd &&
				Objects.equals(methodInfo, that.methodInfo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(methodInfo, insertionBegin, insertionEnd);
	}
}