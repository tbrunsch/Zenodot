package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.compound.AbstractCombineRule;

public class ConstructorClassRule extends AbstractCombineRule<Void, Class<?>, ConstructorParseInfo, JavaSettings>
{
	public ConstructorClassRule(Rule<Void, Class<?>, JavaSettings> classRule) {
		super(classRule);
	}

	@Override
	protected ConstructorParseInfo combine(Void ignored, Class<?> constructorClass, JavaSettings settings) {
		return new ConstructorParseInfo(constructorClass);
	}
}
