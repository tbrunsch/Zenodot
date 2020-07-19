package dd.kms.zenodot.parsers.expectations;

import dd.kms.zenodot.flowcontrol.InternalParseException;
import dd.kms.zenodot.matching.MatchRatings;
import dd.kms.zenodot.matching.TypeMatch;
import dd.kms.zenodot.result.ObjectParseResult;
import dd.kms.zenodot.utils.dataproviders.ObjectInfoProvider;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectParseResultExpectation extends AbstractParseResultExpectation<ObjectParseResult>
{
	private final @Nullable List<TypeInfo>	expectedTypes;
	private final boolean					resultTypeMustMatch;

	public ObjectParseResultExpectation() {
		this(null, false);
	}

	public ObjectParseResultExpectation(@Nullable List<TypeInfo> expectedTypes, boolean resultTypeMustMatch) {
		this(expectedTypes, resultTypeMustMatch, false);
	}

	private ObjectParseResultExpectation(@Nullable List<TypeInfo> expectedTypes, boolean resultTypeMustMatch, boolean parseWholeText) {
		super(ObjectParseResult.class, parseWholeText);
		this.expectedTypes = expectedTypes;
		this.resultTypeMustMatch = resultTypeMustMatch;
	}

	@Override
	public ObjectParseResultExpectation parseWholeText(boolean parseWholeText) {
		return isParseWholeText() == parseWholeText
			? this
			: new ObjectParseResultExpectation(expectedTypes, resultTypeMustMatch, parseWholeText);
	}

	public ObjectParseResultExpectation resultTypeMustMatch(boolean resultTypeMustMatch) {
		return this.resultTypeMustMatch == resultTypeMustMatch
			? this
			: new ObjectParseResultExpectation(expectedTypes, resultTypeMustMatch, isParseWholeText());
	}

	public TypeMatch rateTypeMatch(TypeInfo type) {
		if (expectedTypes == null) {
			return TypeMatch.FULL;
		}
		return expectedTypes.stream()
			.map(expectedType -> MatchRatings.rateTypeMatch(expectedType, type))
			.min(TypeMatch::compareTo)
			.orElse(TypeMatch.NONE);
	}

	@Override
	void doCheck(ObjectParseResult parseResult, ObjectInfoProvider objectInfoProvider) throws InternalParseException {
		TypeInfo resultType = objectInfoProvider.getType(parseResult.getObjectInfo());
		TypeMatch typeMatch = rateTypeMatch(resultType);
		if (typeMatch == TypeMatch.NONE) {
			String messagePrefix = "The class '" + resultType + "' is not assignable to ";
			String messageMiddle = expectedTypes.size() > 1
				? "any of the expected classes "
				: "the expected class ";
			String messageSuffix = "'" + expectedTypes.stream().map(Object::toString).collect(Collectors.joining("', '")) + "'";
			String message = messagePrefix + messageMiddle + messageSuffix;
			throw new InternalParseException(message);
		}
	}
}
