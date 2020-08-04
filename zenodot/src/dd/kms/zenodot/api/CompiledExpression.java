package dd.kms.zenodot.api;

import dd.kms.zenodot.api.wrappers.ObjectInfo;
import dd.kms.zenodot.api.wrappers.TypeInfo;

public interface CompiledExpression
{
	TypeInfo getResultType();
	ObjectInfo evaluate(ObjectInfo thisValue) throws Exception;
}
