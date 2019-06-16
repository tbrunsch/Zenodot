package dd.kms.zenodot;

public interface CompiledExpression
{
	Object evaluate(Object thisValue) throws Exception;
}
