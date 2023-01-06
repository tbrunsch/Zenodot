package dd.kms.zenodot.impl.parsers.expectations;

import dd.kms.zenodot.api.matching.TypeMatch;
import dd.kms.zenodot.impl.common.ObjectInfoProvider;
import dd.kms.zenodot.impl.flowcontrol.SyntaxException;
import dd.kms.zenodot.impl.matching.MatchRatings;
import dd.kms.zenodot.impl.result.ObjectParseResult;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectParseResultExpectation extends AbstractParseResultExpectation<ObjectParseResult>
{
	private final @Nullable List<Class<?>>	expectedTypes;
	private final boolean					resultTypeMustMatch;
	private final @Nullable Class<?>[]		parameterTypes;

	public ObjectParseResultExpectation() {
		this(null, false);
	}

	public ObjectParseResultExpectation(@Nullable List<Class<?>> expectedTypes, boolean resultTypeMustMatch) {
		this(expectedTypes, resultTypeMustMatch, false, null);
	}

	private ObjectParseResultExpectation(@Nullable List<Class<?>> expectedTypes, boolean resultTypeMustMatch, boolean parseWholeText, @Nullable Class<?>[] parameterTypes) {
		super(ObjectParseResult.class, parseWholeText);
		this.expectedTypes = expectedTypes;
		this.resultTypeMustMatch = resultTypeMustMatch;
		this.parameterTypes = parameterTypes != null
			? Arrays.copyOf(parameterTypes, parameterTypes.length)
			: null;
	}

	@Nullable
	public List<Class<?>> getExpectedTypes() {
		return expectedTypes;
	}

	@Nullable
	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	@Override
	public ObjectParseResultExpectation parseWholeText(boolean parseWholeText) {
		return isParseWholeText() == parseWholeText
			? this
			: new ObjectParseResultExpectation(expectedTypes, resultTypeMustMatch, parseWholeText, parameterTypes);
	}

	public ObjectParseResultExpectation resultTypeMustMatch(boolean resultTypeMustMatch) {
		return this.resultTypeMustMatch == resultTypeMustMatch
			? this
			: new ObjectParseResultExpectation(expectedTypes, resultTypeMustMatch, isParseWholeText(), parameterTypes);
	}

	public ObjectParseResultExpectation parameterTypes(Class<?>[] parameterTypes) {
		return new ObjectParseResultExpectation(expectedTypes, resultTypeMustMatch, isParseWholeText(), parameterTypes);
	}

	public TypeMatch rateTypeMatch(Class<?> type) {
		if (expectedTypes == null) {
			return TypeMatch.FULL;
		}
		return expectedTypes.stream()
			.map(expectedType -> MatchRatings.rateTypeMatch(expectedType, type))
			.min(TypeMatch::compareTo)
			.orElse(TypeMatch.NONE);
	}

	@Override
	void doCheck(ObjectParseResult parseResult, ObjectInfoProvider objectInfoProvider) throws SyntaxException {
		if (!resultTypeMustMatch) {
			return;
		}
		Class<?> resultType = objectInfoProvider.getType(parseResult.getObjectInfo());
		TypeMatch typeMatch = rateTypeMatch(resultType);
		if (typeMatch == TypeMatch.NONE) {
			String messagePrefix = "The class '" + resultType + "' is not assignable to ";
			String messageMiddle = expectedTypes.size() > 1
				? "any of the expected classes "
				: "the expected class ";
			String messageSuffix = "'" + expectedTypes.stream().map(Object::toString).collect(Collectors.joining("', '")) + "'";
			String message = messagePrefix + messageMiddle + messageSuffix;
			throw new SyntaxException(message);
		}
	}
}
