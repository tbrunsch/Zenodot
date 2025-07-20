package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.EvaluationRule;
import dd.kms.zenodotx.rule.compound.Pair;

public class AddConstructorParameterRule extends AbstractRule<Pair<ConstructorParseInfo, InstanceParseResult>, ConstructorParseInfo, JavaSettings> implements EvaluationRule<Pair<ConstructorParseInfo, InstanceParseResult>, ConstructorParseInfo, JavaSettings>
{
	@Override
	public ConstructorParseInfo evaluate(Pair<ConstructorParseInfo, InstanceParseResult> pair, JavaSettings settings) throws SemanticException, EvaluationException {
		ConstructorParseInfo constructorParseInfo = pair.getFirst();
		InstanceParseResult parameter = pair.getSecond();
		return constructorParseInfo.addParameter(parameter);
	}

	@Override
	protected String getGenericName() {
		return "new Class(..., parameter, ...)";
	}
}
