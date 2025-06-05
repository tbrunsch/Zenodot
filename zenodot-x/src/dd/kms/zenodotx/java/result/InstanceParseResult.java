package dd.kms.zenodotx.java.result;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.exception.EvaluationException;

public interface InstanceParseResult
{
	ObjectInfo getEvaluatedResult();

	ObjectInfo evaluate(ObjectInfo thisInfo, Variables variables, EvaluationMode evaluationMode) throws EvaluationException;
}
