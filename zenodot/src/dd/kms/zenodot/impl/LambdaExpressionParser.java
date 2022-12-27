package dd.kms.zenodot.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import dd.kms.zenodot.api.common.ReflectionUtils;
import dd.kms.zenodot.api.settings.ParserSettings;
import dd.kms.zenodot.impl.parsers.expectations.ObjectParseResultExpectation;

class LambdaExpressionParser extends ExpressionParserImpl
{
	private final Class<?>	functionalInterface;

	LambdaExpressionParser(ParserSettings settings, VariablesImpl variables, Class<?> functionalInterface) {
		super(settings, variables);
		Preconditions.checkArgument(ReflectionUtils.isFunctionalInterface(functionalInterface),
			"Class '" + functionalInterface.getName() + "' is no functional interface");
		this.functionalInterface = functionalInterface;
	}

	@Override
	ObjectParseResultExpectation getParseResultExpectation() {
		return new ObjectParseResultExpectation(ImmutableList.of(functionalInterface), true)
			.parseWholeText(true);
	}
}
