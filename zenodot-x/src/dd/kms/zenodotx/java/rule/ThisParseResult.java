package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.java.result.InstanceParseResult;

public class ThisParseResult implements InstanceParseResult
{
	private final ObjectInfo evaluatedResult;

	public ThisParseResult(ObjectInfo thisInfo) {
		this.evaluatedResult = thisInfo;
	}

	@Override
	public ObjectInfo getEvaluatedResult() {
		return evaluatedResult;
	}

	@Override
	public ObjectInfo evaluate(ObjectInfo thisInfo, Variables variables, EvaluationMode evaluationMode) {
		return thisInfo;
	}
}
