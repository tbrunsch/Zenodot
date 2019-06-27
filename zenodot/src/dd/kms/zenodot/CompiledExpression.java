package dd.kms.zenodot;

import dd.kms.zenodot.utils.wrappers.TypeInfo;

public interface CompiledExpression
{
	TypeInfo getResultType();
	Object evaluate(Object thisValue) throws Exception;
}
