package dd.kms.zenodotx.java.rule;

import dd.kms.zenodot.api.Variables;
import dd.kms.zenodot.api.settings.EvaluationMode;
import dd.kms.zenodot.framework.wrappers.ObjectInfo;
import dd.kms.zenodot.impl.VariablesImpl;
import dd.kms.zenodotx.java.result.InstanceParseResult;

public class VariableParseResult implements InstanceParseResult
{
	private final String		variableName;
	private final ObjectInfo	evaluatedResult;

	public VariableParseResult(String variableName, ObjectInfo valueInfo) {
		this.variableName = variableName;
		this.evaluatedResult = valueInfo;
	}

	@Override
	public ObjectInfo getEvaluatedResult() {
		return evaluatedResult;
	}

	@Override
	public ObjectInfo evaluate(ObjectInfo thisInfo, Variables variables, EvaluationMode evaluationMode) {
		return ((VariablesImpl) variables).getValueInfo(variableName);
	}
}
