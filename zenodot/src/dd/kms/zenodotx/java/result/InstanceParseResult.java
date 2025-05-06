package dd.kms.zenodotx.java.result;

import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.java.JavaSettings;

public interface InstanceParseResult
{
	ObjectInfo getEvaluatedResult();
	ObjectInfo evaluate(JavaSettings settings) throws EvaluationException;
}
