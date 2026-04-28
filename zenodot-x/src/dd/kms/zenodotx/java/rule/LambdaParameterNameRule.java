package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

public class LambdaParameterNameRule extends AbstractRule<LambdaParameterInfo, LambdaParameterInfo, JavaSettings> implements SimpleRule<LambdaParameterInfo, LambdaParameterInfo, JavaSettings>
{
	@Override
	public SyntaxRule getSyntaxRule() {
		return IdentifierSyntaxRule.RULE;
	}

	@Override
	public SemanticRule<LambdaParameterInfo, LambdaParameterInfo, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<LambdaParameterInfo, LambdaParameterInfo>() {
			@Override
			protected void doSuggestCodeCompletions(LambdaParameterInfo parameterInfo, String parsedString, JavaSettings settings) {
				// TODO
			}

			@Override
			protected void doSuggestMethodParameters(LambdaParameterInfo parameterInfo, JavaSettings settings) {
				// TODO
			}

			@Override
			public LambdaParameterInfo evaluate(LambdaParameterInfo parameterInfo, String parameterName, JavaSettings settings) throws SemanticException, EvaluationException {
				parameterInfo.addParameter(parameterName);
				return parameterInfo;
			}
		};
	}
}
