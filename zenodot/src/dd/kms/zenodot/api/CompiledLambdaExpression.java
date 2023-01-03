package dd.kms.zenodot.api;

public interface CompiledLambdaExpression<T> extends CompiledExpression
{
	T evaluate(Object thisValue) throws Exception;
	Class<?> getLambdaResultType();
}
