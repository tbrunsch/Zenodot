package dd.kms.zenodotx.rule.simple;

import dd.kms.zenodotx.event.Event;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.state.State;

public class EmptyRule<S extends State<S>> implements SimpleRule<S>
{
	@Override
	public SyntaxRule getSyntaxRule() {
		return SyntaxRules.EMPTY;
	}

	@Override
	public SemanticRule<S> getSemanticRule() {
		return new SemanticRule<S>() {
			@Override
			public void evaluate(String parsedString, S state) throws SemanticException, EvaluationException {
				/* don't do anything */
			}

			@Override
			public void handleEvent(Event event, String parsedString, S state) {
				/* don't do anything */
			}
		};
	}
}
