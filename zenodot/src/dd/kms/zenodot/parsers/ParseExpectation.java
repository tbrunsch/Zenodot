package dd.kms.zenodot.parsers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dd.kms.zenodot.result.ParseResultType;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;
import java.util.Set;

/**
 * Describes the expectation of the result of parsing a certain (sub-) expression. This
 * is used for checking the result of a parser and for rating completing suggestions.
 */
public class ParseExpectation
{
	public static final Set<ParseResultType>	SUPPORTED_RESULT_TYPES	= ImmutableSet.of(
		ParseResultType.OBJECT_PARSE_RESULT,
		ParseResultType.CLASS_PARSE_RESULT,
		ParseResultType.PACKAGE_PARSE_RESULT
	);

	public static final ParseExpectation		CLASS					= ParseExpectationBuilder.expectClass().build();
	public static final ParseExpectation		PACKAGE					= ParseExpectationBuilder.expectPackage().build();
	public static final ParseExpectation		OBJECT					= ParseExpectationBuilder.expectObject().build();

	private final ParseResultType	evaluationType;
	private final List<TypeInfo>	allowedTypes;

	ParseExpectation(ParseResultType evaluationType, List<TypeInfo> allowedTypes) {
		if (!SUPPORTED_RESULT_TYPES.contains(evaluationType)) {
			throw new IllegalArgumentException("Only objects, classes, and packages can be expected as valid code evaluation types");
		}
		this.evaluationType = evaluationType;
		this.allowedTypes = allowedTypes == null ? null : ImmutableList.copyOf(allowedTypes);
	}

	public ParseResultType getEvaluationType() {
		return evaluationType;
	}

	public List<TypeInfo> getAllowedTypes() {
		return allowedTypes;
	}
}
