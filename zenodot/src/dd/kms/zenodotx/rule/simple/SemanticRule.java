package dd.kms.zenodotx.rule.simple;

import dd.kms.zenodotx.event.Event;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;
import dd.kms.zenodotx.state.State;

public interface SemanticRule<S extends State>
{
	void evaluate(String parsedString, S state) throws SemanticException, EvaluationException;

	// Used, e.g., for code completion
	void handleEvent(Event event, String parsedString, S state);
}
