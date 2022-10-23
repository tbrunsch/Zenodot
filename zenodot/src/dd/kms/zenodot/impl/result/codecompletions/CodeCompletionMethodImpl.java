package dd.kms.zenodot.impl.result.codecompletions;

import dd.kms.zenodot.api.matching.MatchRating;
import dd.kms.zenodot.api.result.CodeCompletionType;
import dd.kms.zenodot.api.result.codecompletions.CodeCompletionMethod;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class CodeCompletionMethodImpl extends AbstractCodeCompletion implements CodeCompletionMethod
{
	private final Method	method;

	CodeCompletionMethodImpl(Method method, int insertionBegin, int insertionEnd, MatchRating rating) {
		super(CodeCompletionType.METHOD, insertionBegin, insertionEnd, rating);
		this.method = method;
	}

	@Override
	public Method getMethod() {
		return method;
	}

	@Override
	public int getCaretPositionAfterInsertion() {
		boolean hasArguments = method.getParameterCount() > 0;
		int insertionBegin = getInsertionRange().getBegin();
		return insertionBegin + (hasArguments
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
