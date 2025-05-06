package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.Parser;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.exception.SyntaxException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.Rule;
import dd.kms.zenodotx.rule.compound.CompoundRule;

public class ConstructorClassRule extends AbstractRule<Void, ConstructorParseInfo, JavaSettings> implements CompoundRule<Void, ConstructorParseInfo, JavaSettings>
{
	private final Rule<Void, Class<?>, JavaSettings>	classRule;

	public ConstructorClassRule(Rule<Void, Class<?>, JavaSettings> classRule) {
		this.classRule = classRule;
	}

	@Override
	public ConstructorParseInfo parse(Void input, JavaSettings settings, Parser<JavaSettings> parser) throws SyntaxException, EvaluationException, SemanticException, Parser.EventResultException {
		Class<?> constructorClass = parser.parse(classRule, input, settings);
		return new ConstructorParseInfo(constructorClass);
	}
}
