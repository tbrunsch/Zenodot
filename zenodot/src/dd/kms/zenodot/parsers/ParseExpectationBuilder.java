package dd.kms.zenodot.parsers;

import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.result.ParseResultType;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

import java.util.List;

/**
 * Builder for {@link ParseExpectation}
 */
public class ParseExpectationBuilder
{
	public static ParseExpectationBuilder expectObject() {
		return new ParseExpectationBuilder(ParseResultType.OBJECT_PARSE_RESULT);
	}

	public static ParseExpectationBuilder expectClass() {
		return new ParseExpectationBuilder(ParseResultType.CLASS_PARSE_RESULT);
	}

	public static ParseExpectationBuilder expectPackage() {
		return new ParseExpectationBuilder(ParseResultType.PACKAGE_PARSE_RESULT);
	}

	private final ParseResultType	evaluationType;
	private List<TypeInfo>			allowedTypes;

	ParseExpectationBuilder(ParseResultType evaluationType) {
		this.evaluationType = evaluationType;
	}

	public ParseExpectationBuilder allowedTypes(List<TypeInfo> allowedTypes) {
		this.allowedTypes = ImmutableList.copyOf(allowedTypes);
		return this;
	}

	public ParseExpectationBuilder allowedType(TypeInfo allowedType) {
		this.allowedTypes = ImmutableList.of(allowedType);
		return this;
	}

	public ParseExpectation build() {
		return new ParseExpectation(evaluationType, allowedTypes);
	}
}
