package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.EvaluationRule;
import dd.kms.zenodotx.rule.compound.Pair;

public class AddMethodParameterRule extends AbstractRule<Pair<ExecutableParseInfo, InstanceParseResult>, ExecutableParseInfo, JavaSettings> implements EvaluationRule<Pair<ExecutableParseInfo, InstanceParseResult>, ExecutableParseInfo, JavaSettings>
{
	@Override
	public ExecutableParseInfo evaluate(Pair<ExecutableParseInfo, InstanceParseResult> pair, JavaSettings settings) throws SemanticException, EvaluationException {
		ExecutableParseInfo executableParseInfo = pair.getFirst();
		InstanceParseResult parameter = pair.getSecond();
		return executableParseInfo.addParameter(parameter);
	}

	@Override
	protected String getGenericName() {
		return "method(..., parameter, ...)";
	}
}
