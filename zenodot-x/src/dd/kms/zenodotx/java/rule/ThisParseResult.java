package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;

class ThisParseResult implements InstanceParseResult {
	private final ObjectInfo evaluatedResult;

	ThisParseResult(JavaSettings settings) {
		this.evaluatedResult = evaluate(settings);
	}

	@Override
	public ObjectInfo getEvaluatedResult() {
		return evaluatedResult;
	}

	@Override
	public ObjectInfo evaluate(JavaSettings settings) {
		return settings.getThisInfo();
	}
}
