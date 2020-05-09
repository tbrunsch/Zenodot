package dd.kms.zenodot.parsers;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.result.ParseResultType;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;

/**
 * Describes the expectation of the result of parsing a certain (sub-) expression. This
 * is used for checking the result of a parser and for rating code completions.
 */
public class ParseExpectation
{
	public static final ParseExpectation	CLASS		= ParseExpectationBuilder.expectClass().build();
	public static final ParseExpectation	PACKAGE		= ParseExpectationBuilder.expectPackage().build();
	public static final ParseExpectation	OBJECT		= ParseExpectationBuilder.expectObject().build();

	private final ParseResultType	resultType;
	private final List<TypeInfo>	allowedTypes;

	ParseExpectation(ParseResultType resultType, List<TypeInfo> allowedTypes) {
		this.resultType = resultType;
		this.allowedTypes = allowedTypes == null ? null : ImmutableList.copyOf(allowedTypes);
	}

	public ParseResultType getResultType() {
		return resultType;
	}

	public List<TypeInfo> getAllowedTypes() {
		return allowedTypes;
	}
}
