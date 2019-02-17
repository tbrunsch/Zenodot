package com.AMS.jBEAM.javaParser.parsers;

import com.AMS.jBEAM.javaParser.result.ParseResultType;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;

import java.util.List;

public class ParseExpectation
{
	public static final ParseExpectation	CLASS	= ParseExpectationBuilder.expectClass().build();
	public static final ParseExpectation	OBJECT	= ParseExpectationBuilder.expectObject().build();

	private final ParseResultType		evaluationType;
	private final List<TypeToken<?>>	allowedTypes;

	ParseExpectation(ParseResultType evaluationType, List<TypeToken<?>> allowedTypes) {
		if (evaluationType != ParseResultType.OBJECT_PARSE_RESULT && evaluationType != ParseResultType.CLASS_PARSE_RESULT) {
			throw new IllegalArgumentException("Only objects and classes can be expected as valid code evaluation types");
		}
		this.evaluationType = evaluationType;
		this.allowedTypes = allowedTypes == null ? null : ImmutableList.copyOf(allowedTypes);
	}

	public ParseResultType getEvaluationType() {
		return evaluationType;
	}

	public List<TypeToken<?>> getAllowedTypes() {
		return allowedTypes;
	}

	public ParseExpectationBuilder builder() {
		return new ParseExpectationBuilder(evaluationType).allowedTypes(allowedTypes);
	}
}
