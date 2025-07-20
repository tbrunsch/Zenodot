package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.EvaluationRule;

public class BeginLambdaRule extends AbstractRule<Void, LambdaParameterInfo, JavaSettings> implements EvaluationRule<Void, LambdaParameterInfo, JavaSettings>
{
	@Override
	public LambdaParameterInfo evaluate(Void input, JavaSettings settings) throws SemanticException, EvaluationException {
		return new LambdaParameterInfo();
	}
}
