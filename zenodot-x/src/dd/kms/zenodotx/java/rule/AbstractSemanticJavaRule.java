package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.event.Event;
import dd.kms.zenodotx.java.JavaSettings;
import dd.kms.zenodotx.java.events.JavaEvent;
import dd.kms.zenodotx.rule.simple.SemanticRule;

public abstract class AbstractSemanticJavaRule<I, O> implements SemanticRule<I, O, JavaSettings>
{
	protected abstract void doSuggestCodeCompletions(I input, String parsedString, JavaSettings settings);
	protected abstract void doSuggestMethodParameters(I input, JavaSettings settings);

	@Override
	public final void handleEvent(Event event, I input, String parsedString, JavaSettings settings) {
		if (event == JavaEvent.CODE_COMPLETION) {
			doSuggestCodeCompletions(input, parsedString, settings);
		} else if (event == JavaEvent.METHOD_PARAMETER_SUGGESTION) {
			doSuggestMethodParameters(input, settings);
		} else {
			throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
		}
	}
}
