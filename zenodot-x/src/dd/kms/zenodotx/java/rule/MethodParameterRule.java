package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.compound.AbstractCombineRule;

public class MethodParameterRule extends AbstractCombineRule<ExecutableParseInfo, InstanceParseResult, ExecutableParseInfo, JavaSettings>
{
	public MethodParameterRule(Rule<Void, InstanceParseResult, JavaSettings> expressionRule) {
		super(expressionRule);
	}

	@Override
	protected ExecutableParseInfo combine(ExecutableParseInfo executableParseInfo, InstanceParseResult parameter, JavaSettings settings) {
		executableParseInfo.addParameter(parameter);
		return executableParseInfo;
	}

	@Override
	protected String getGenericName() {
		return "method(..., parameter, ...)";
	}
}
