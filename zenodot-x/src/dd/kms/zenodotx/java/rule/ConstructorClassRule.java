package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.EvaluationRule;

public class ConstructorClassRule extends AbstractRule<Class<?>, ConstructorParseInfo, JavaSettings> implements EvaluationRule<Class<?>, ConstructorParseInfo, JavaSettings>
{
	@Override
	public ConstructorParseInfo evaluate(Class<?> constructorClass, JavaSettings settings) throws SemanticException, EvaluationException {
		return new ConstructorParseInfo(constructorClass);
	}
}
