package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.result.InstanceParseResult;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;
import dd.kms.zenodotx.rule.simple.SyntaxRules;

public class InvokeInstanceConstructorRule extends AbstractRule<ConstructorParseInfo, InstanceParseResult, JavaSettings> implements SimpleRule<ConstructorParseInfo, InstanceParseResult, JavaSettings>
{
	@Override
	public SyntaxRule getSyntaxRule() {
		return SyntaxRules.EMPTY;
	}

	@Override
	public SemanticRule<ConstructorParseInfo, InstanceParseResult, JavaSettings> getSemanticRule() {
		return new AbstractSemanticJavaRule<ConstructorParseInfo, InstanceParseResult>() {
			@Override
			protected void doSuggestCodeCompletions(ConstructorParseInfo input, String parsedString, JavaSettings settings) {
				/* nothing to do */
			}

			@Override
			protected void doSuggestMethodParameters(ConstructorParseInfo input, JavaSettings settings) {
				/* nothing to do */
			}

			@Override
			public InstanceParseResult evaluate(ConstructorParseInfo constructorParseInfo, String parsedString, JavaSettings settings) throws SemanticException, EvaluationException {
				// TODO: Finish InvokeMethodRule and then copy things from it
				return null;
			}
		};
	}

	@Override
	protected String getGenericName() {
		return "evaluate new Class(...)";
	}
}
