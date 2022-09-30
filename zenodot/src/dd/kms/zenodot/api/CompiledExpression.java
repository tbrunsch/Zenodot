package dd.kms.zenodot.api;

import dd.kms.zenodot.api.wrappers.ObjectInfo;

public interface CompiledExpression
{
	Class<?> getResultType();
	ObjectInfo evaluate(ObjectInfo thisValue) throws Exception;
}
