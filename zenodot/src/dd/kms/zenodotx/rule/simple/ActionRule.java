package dd.kms.zenodotx.rule.simple;

import dd.kms.zenodotx.event.Event;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.state.State;

import java.util.function.Consumer;

public class ActionRule<S extends State<S>> implements SimpleRule<S>
{
	private final Consumer<S>	action;

	public ActionRule(Consumer<S> action) {
		this.action = action;
	}

	@Override
	public SyntaxRule getSyntaxRule() {
		return SyntaxRules.EMPTY;
	}

	@Override
	public SemanticRule<S> getSemanticRule() {
		return new SemanticRule<S>() {
			@Override
			public void evaluate(String parsedString, S state) throws SemanticException, EvaluationException {
				action.accept(state);
			}

			@Override
			public void handleEvent(Event event, String parsedString, S state) {
				/* don't do anything */
			}
		};
	}
}
