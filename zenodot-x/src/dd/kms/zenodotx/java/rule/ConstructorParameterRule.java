package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.compound.AbstractCombineRule;

public class ConstructorParameterRule extends AbstractCombineRule<ConstructorParseInfo, InstanceParseResult, ConstructorParseInfo, JavaSettings>
{
	public ConstructorParameterRule(Rule<Void, InstanceParseResult, JavaSettings> expressionRule) {
		super(expressionRule);
	}

	@Override
	protected ConstructorParseInfo combine(ConstructorParseInfo constructorParseInfo, InstanceParseResult parameter, JavaSettings settings) {
		constructorParseInfo.addParameter(parameter);
		return constructorParseInfo;
	}

	@Override
	protected String getGenericName() {
		return "new Class(..., parameter, ...)";
	}
}
