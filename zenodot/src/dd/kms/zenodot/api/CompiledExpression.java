package dd.kms.zenodot.api;

public interface CompiledExpression
{
	Class<?> getResultType();
	Object evaluate(Object thisValue) throws Exception;
}
