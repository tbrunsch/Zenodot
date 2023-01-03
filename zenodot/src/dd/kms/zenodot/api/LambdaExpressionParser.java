package dd.kms.zenodot.api;

public interface LambdaExpressionParser<T> extends ExpressionParser
{
	/**
	 * Evaluates the expression in the context provided by {@code thisValue}.
	 */
	@Override
	T evaluate(String expression, Object thisValue) throws ParseException;

	/**
	 * Compiles the expression in the context provided by {@code thisType}.
	 */
	CompiledLambdaExpression<T> compile(String expression, Class<?> thisType) throws ParseException;

	/**
	 * Compiles the expression in the context provided by {@code thisValue}.<br>
	 * <br>
	 * This method requires information about an object instead of a class. The reason
	 * is that with {@link dd.kms.zenodot.api.settings.EvaluationMode#DYNAMIC_TYPING} or
	 * {@link dd.kms.zenodot.api.settings.EvaluationMode#MIXED} also runtime type information
	 * will be considered. If you want to compile an expression based on a class, then you
	 * can call {@link #compile(String, Class)} instead.
	 */
	CompiledLambdaExpression<T> compile(String expression, Object thisValue) throws ParseException;
}
