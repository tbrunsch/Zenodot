package dd.kms.zenodotx.java.rule;

import dd.kms.zenodotx.event.Event;
import dd.kms.zenodotx.java.JavaState;
import dd.kms.zenodotx.java.events.JavaEvent;
import dd.kms.zenodotx.rule.simple.SemanticRule;

public abstract class AbstractSemanticJavaRule implements SemanticRule<JavaState>
{
	abstract void doSuggestCodeCompletions(String parsedString, JavaState state);
	abstract void doSuggestMethodParameters(JavaState state);

	@Override
	public final void handleEvent(Event event, String parsedString, JavaState state) {
		if (event == JavaEvent.CODE_COMPLETION) {
			doSuggestCodeCompletions(parsedString, state);
		} else if (event == JavaEvent.METHOD_PARAMETER_SUGGESTION) {
			doSuggestMethodParameters(state);
		} else {
			throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
		}
	}
}
