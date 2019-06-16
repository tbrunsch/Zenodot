package dd.kms.zenodot.result;

import dd.kms.zenodot.utils.wrappers.ObjectInfo;

public interface CompiledObjectParseResult extends ObjectParseResult
{
	ObjectInfo evaluate(ObjectInfo thisInfo, ObjectInfo context) throws Exception;
}
