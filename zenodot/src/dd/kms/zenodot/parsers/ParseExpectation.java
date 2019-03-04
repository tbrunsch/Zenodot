package dd.kms.zenodot.parsers;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.result.ParseResultType;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;

/**
 * Describes the expectation of the result of parsing a certain (sub-) expression. This
 * is used for checking the result of a parser and for rating completing suggestions.
 */
public class ParseExpectation
{
	public static final ParseExpectation	CLASS	= ParseExpectationBuilder.expectClass().build();
	public static final ParseExpectation	OBJECT	= ParseExpectationBuilder.expectObject().build();

	private final ParseResultType	evaluationType;
	private final List<TypeInfo>	allowedTypes;

	ParseExpectation(ParseResultType evaluationType, List<TypeInfo> allowedTypes) {
		if (evaluationType != ParseResultType.OBJECT_PARSE_RESULT && evaluationType != ParseResultType.CLASS_PARSE_RESULT) {
			throw new IllegalArgumentException("Only objects and classes can be expected as valid code evaluation types");
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
