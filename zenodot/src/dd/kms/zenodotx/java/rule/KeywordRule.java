package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaState;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;
import dd.kms.zenodotx.rule.simple.SyntaxRules;

public class KeywordRule implements SimpleRule<JavaState>
{
	private final String	keyword;

	public KeywordRule(String keyword) {
		this.keyword = keyword;
	}

	@Override
	public SyntaxRule getSyntaxRule() {
		return SyntaxRules.forString(keyword);
	}

	@Override
	public SemanticRule<JavaState> getSemanticRule() {
		return new AbstractSemanticJavaRule()
		{
			@Override
			public void evaluate(String parsedString, JavaState state) throws SemanticException, EvaluationException {
				/* nothing to do */
			}

			@Override
			void doSuggestCodeCompletions(String parsedString, JavaState state) {
				// TODO
			}

			@Override
			void doSuggestMethodParameters(JavaState state) {
				// TODO
			}
		};
	}
}
