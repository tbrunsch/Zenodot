package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;
import dd.kms.zenodotx.rule.simple.SyntaxRules;

public class BeginLambdaRule extends AbstractRule<Void, LambdaParameterInfo, JavaSettings> implements SimpleRule<Void, LambdaParameterInfo, JavaSettings>
{
	@Override
	public SyntaxRule getSyntaxRule() {
		return SyntaxRules.EMPTY;
	}

	@Override
	public SemanticRule<Void, LambdaParameterInfo, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<Void, LambdaParameterInfo>() {
			@Override
			protected void doSuggestCodeCompletions(Void input, String parsedString, JavaSettings settings) {
				/* nothing to do */
				return;
			}

			@Override
			protected void doSuggestMethodParameters(Void input, JavaSettings settings) {
				/* nothing to do */
				return;
			}

			@Override
			public LambdaParameterInfo evaluate(Void input, String parsedString, JavaSettings settings) {
				return new LambdaParameterInfo();
			}
		};
	}
}
