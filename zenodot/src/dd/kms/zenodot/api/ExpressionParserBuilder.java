package dd.kms.zenodot.api;

public interface ExpressionParserBuilder
{
	/**
	 * Registers the variables that can be accessed and possibly overwritten by the expression to parse.
	 */
	ExpressionParserBuilder variables(Variables variables);

	ExpressionParser createExpressionParser();

	/**
	 * Creates a parser for a lambda that represents the functional interface {@code functionalInterface}.
	 */
	<T> LambdaExpressionParser<T> createLambdaParser(Class<T> functionalInterface);

	/**
	 * Creates a parser for a lambda that represents the functional interface {@code functionalInterface}.
	 * With {@code parameterTypes} you can specify the exact types of the parameters of the method to
	 * implement. This is necessary if the functional interface is generic or extends a generic interface
	 * in order to avoid casts in the lambda body.
	 */
	<T> LambdaExpressionParser<T> createLambdaParser(Class<T> functionalInterface, Class<?>... parameterTypes);
}
