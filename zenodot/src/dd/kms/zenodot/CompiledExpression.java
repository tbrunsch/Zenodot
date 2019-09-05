package dd.kms.zenodot;

import dd.kms.zenodot.utils.wrappers.ObjectInfo;
import dd.kms.zenodot.utils.wrappers.TypeInfo;

public interface CompiledExpression
{
	TypeInfo getResultType();
	ObjectInfo evaluate(ObjectInfo thisValue) throws Exception;
}
