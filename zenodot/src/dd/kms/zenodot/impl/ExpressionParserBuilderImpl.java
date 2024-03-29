package dd.kms.zenodot.impl;

import dd.kms.zenodot.api.ExpressionParser;
import dd.kms.zenodot.api.ExpressionParserBuilder;
import dd.kms.zenodot.api.LambdaExpressionParser;
import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.settings.ParserSettings;

public class ExpressionParserBuilderImpl implements ExpressionParserBuilder
{
	private final ParserSettings	parserSettings;

	private Variables				variables		= Variables.create();

	public ExpressionParserBuilderImpl(ParserSettings parserSettings) {
		this.parserSettings = parserSettings;
	}

	@Override
	public ExpressionParserBuilder variables(Variables variables) {
		if (!(variables instanceof VariablesImpl)) {
			throw new IllegalArgumentException("Parameter 'variables' is of a custom type '" + variables.getClass() + "'. "
				+ "This is not supported. Use Variables.create() to create instances of Variables");
		}
		this.variables = variables;
		return this;
	}

	@Override
	public ExpressionParser createExpressionParser() {
		return new ExpressionParserImpl(parserSettings, (VariablesImpl) variables);
	}

	@Override
	public <T> LambdaExpressionParser<T> createLambdaParser(Class<T> functionalInterface) {
		return new LambdaExpressionParserImpl<>(parserSettings, (VariablesImpl) variables, functionalInterface, null);
	}

	@Override
	public <T> LambdaExpressionParser<T> createLambdaParser(Class<T> functionalInterface, Class<?>... parameterTypes) {
		return new LambdaExpressionParserImpl<>(parserSettings, (VariablesImpl) variables, functionalInterface, parameterTypes);
	}
}
