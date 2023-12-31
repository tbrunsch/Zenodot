package dd.kms.zenodot.impl.result.codecompletions;

import dd.kms.zenodot.api.common.GeneralizedMethod;
import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionMethod;
import dd.kms.zenodot.framework.result.codecompletions.AbstractCodeCompletion;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class CodeCompletionMethodImpl extends AbstractCodeCompletion implements CodeCompletionMethod
{
	private final GeneralizedMethod method;

	CodeCompletionMethodImpl(GeneralizedMethod method, int insertionBegin, int insertionEnd, MatchRating rating) {
		super(CodeCompletionType.METHOD, insertionBegin, insertionEnd, rating);
		this.method = method;
	}

	@Override
	public GeneralizedMethod getMethod() {
		return method;
	}

	@Override
	public int getCaretPositionAfterInsertion() {
		boolean hasArguments = method.getParameterCount() > 0;
		return getInsertionBegin() + (hasArguments
				? method.getName().length() + 1
				: getTextToInsert().length());
	}

	@Override
	public String getTextToInsert() {
		return method.getName() + "()";
	}

	@Override
	public String toString() {
		final String argumentsAsString;
		Class<?>[] parameterTypes = method.getParameterTypes();
		int numParameters = parameterTypes.length;
		if (method.isVarArgs()) {
			int lastArgumentIndex = numParameters - 1;
			argumentsAsString = IntStream.range(0, numParameters)
				.mapToObj(i -> parameterTypes[i].getSimpleName() + (i == lastArgumentIndex ? "..." : ""))
				.collect(Collectors.joining(", "));
		} else {
			argumentsAsString = IntStream.range(0, numParameters)
				.mapToObj(i -> parameterTypes[i].getSimpleName())
				.collect(Collectors.joining(", "));
		}
		return method.getName()
			+ "("
			+ argumentsAsString
			+ ")";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		CodeCompletionMethodImpl that = (CodeCompletionMethodImpl) o;
		return Objects.equals(method, that.method);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), method);
	}
}
