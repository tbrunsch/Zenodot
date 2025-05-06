package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.event.Event;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.rule.AbstractRule;
import dd.kms.zenodotx.rule.simple.SemanticRule;
import dd.kms.zenodotx.rule.simple.SimpleRule;
import dd.kms.zenodotx.rule.simple.SyntaxRule;

import java.util.regex.Pattern;

// TODO: Remove after all rules have been implemented
class UnimplementedRule<I, O> extends AbstractRule<I, O, JavaSettings> implements SimpleRule<I, O, JavaSettings>
{
	@Override
	public SyntaxRule getSyntaxRule() {
		return UnimplementedSyntaxRule.RULE;
	}

	@Override
	public SemanticRule<I, O, JavaSettings> getSemanticRule() {
		return new UnimplementedSemanticRule<>();
	}

	private static class UnimplementedSyntaxRule implements SyntaxRule
	{
		static SyntaxRule	RULE	= new UnimplementedSyntaxRule();

		@Override
		public Pattern getRegex() {
			return Pattern.compile("---");
		}
	}

	private static class UnimplementedSemanticRule<I, O> implements SemanticRule<I, O, JavaSettings>
	{
		@Override
		public O evaluate(I input, String parsedString, JavaSettings settings) {
			return null;
		}

		@Override
		public void handleEvent(Event event, I input, String parsedString, JavaSettings settings) {
			/* don't do anything */
		}
	}
}
