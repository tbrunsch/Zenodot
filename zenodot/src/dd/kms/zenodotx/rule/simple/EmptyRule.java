package dd.kms.zenodotx.rule.simple;

import dd.kms.zenodotx.event.Event;
import dd.kms.zenodotx.rule.AbstractRule;

public class EmptyRule<IO, S> extends AbstractRule<IO, IO, S> implements SimpleRule<IO, IO, S>
{
	@Override
	public SyntaxRule getSyntaxRule() {
		return SyntaxRules.EMPTY;
	}

	@Override
	public SemanticRule<IO, IO, S> getSemanticRule() {
		return new SemanticRule<IO, IO, S>() {
			@Override
			public IO evaluate(IO input, String parsedString, S settings) {
				return input;
			}

			@Override
			public void handleEvent(Event event, IO input, String parsedString, S state) {
				/* don't do anything */
			}
		};
	}
}
