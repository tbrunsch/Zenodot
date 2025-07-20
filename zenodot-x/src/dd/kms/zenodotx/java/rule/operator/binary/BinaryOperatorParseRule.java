package dd.kms.zenodotx.java.rule.operator.binary;

import dd.kms.zenodotx.java.rule.UnimplementedRule;

public class BinaryOperatorParseRule extends UnimplementedRule<Void, String>
{
	private final BinaryOperatorRegistry	registry;

	public BinaryOperatorParseRule(BinaryOperatorRegistry registry) {
		this.registry = registry;
	}

	@Override
	protected String getGenericName() {
		return "binary operator";
	}
}
