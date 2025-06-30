package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.java.result.InstanceParseResult;

class ConstantParseResult implements InstanceParseResult
{
	private final ObjectInfo	constantInfo;

	ConstantParseResult(ObjectInfo constantInfo) {
		this.constantInfo = constantInfo;
	}

	@Override
	public ObjectInfo getEvaluatedResult() {
		return constantInfo;
	}

	@Override
	public ObjectInfo evaluate(ObjectInfo thisInfo, Variables variables, EvaluationMode evaluationMode) {
		return constantInfo;
	}
}
