package dd.kms.zenodotx.rule.simple;

import dd.kms.zenodotx.event.Event;
import dd.kms.zenodotx.exception.EvaluationException;
import dd.kms.zenodotx.exception.SemanticException;

public interface SemanticRule<I, O, S>
{
	O evaluate(I input, String parsedString, S settings) throws SemanticException, EvaluationException;

	// Used, e.g., for code completion
	void handleEvent(Event event, I input, String parsedString, S settings);
}
