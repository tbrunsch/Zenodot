package dd.kms.zenodot.api;

public interface ExpressionParserBuilder
{
	ExpressionParserBuilder variables(Variables variables);
	ExpressionParser createExpressionParser();
	ExpressionParser createLambdaParser(Class<?> functionalInterface);
	ExpressionParser createLambdaParser(Class<?> functionalInterface, Class<?>... parameterTypes);
}
