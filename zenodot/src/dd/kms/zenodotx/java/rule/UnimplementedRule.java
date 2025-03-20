package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.event.Event;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.java.JavaState;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

import java.util.regex.Pattern;

// TODO: Remove after all rules have been implemented
class UnimplementedRule implements SimpleRule<JavaState>
{
	@Override
	public SyntaxRule getSyntaxRule() {
		return UnimplementedSyntaxRule.RULE;
	}

	@Override
	public SemanticRule<JavaState> getSemanticRule() {
		return null;
	}

	private static class UnimplementedSyntaxRule implements SyntaxRule
	{
		static SyntaxRule	RULE	= new UnimplementedSyntaxRule();

		@Override
		public Pattern getRegex() {
			return Pattern.compile("---");
		}
	}

	private static class UnimplementedSemanticRule implements SemanticRule<JavaState>
	{
		@Override
		public void evaluate(String parsedString, JavaState state) throws SemanticException, EvaluationException {
			/* don't do anything */
		}

		@Override
		public void handleEvent(Event event, String parsedString, JavaState state) {
			/* don't do anything */
		}
	}
}
